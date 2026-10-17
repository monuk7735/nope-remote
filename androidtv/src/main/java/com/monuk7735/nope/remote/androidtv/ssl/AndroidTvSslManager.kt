package com.monuk7735.nope.remote.androidtv.ssl

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Date
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder

class AndroidTvSslManager(private val context: Context) {

    companion object {
        private const val KEYSTORE_NAME = "androidtv_keystore.p12"
        private const val KEY_ALIAS = "atv_client_identity"
        private val KEYSTORE_PASSWORD = "noperemote_atv_keystore".toCharArray()

        @Volatile
        private var instance: AndroidTvSslManager? = null

        fun getInstance(context: Context): AndroidTvSslManager = instance ?: synchronized(this) {
            instance ?: AndroidTvSslManager(context.applicationContext).also {
                instance = it
            }
        }

        fun clearAllPairingData(context: Context): Boolean {
            synchronized(this) {
                instance = null
            }
            val keystoreFile = File(context.filesDir, KEYSTORE_NAME)
            try {
                val sslContext = SSLContext.getInstance("TLS")
                val sessionContext = sslContext.clientSessionContext
                val ids = sessionContext.ids
                while (ids.hasMoreElements()) {
                    sessionContext.getSession(ids.nextElement())?.invalidate()
                }
            } catch (_: Exception) {}
            return if (keystoreFile.exists()) {
                keystoreFile.delete()
            } else {
                true
            }
        }

        fun stripLeadingZeros(bytes: ByteArray): ByteArray {
            var index = 0
            while (index < bytes.size && bytes[index] == 0.toByte()) {
                index++
            }
            if (index == bytes.size) {
                return byteArrayOf(0)
            }
            return bytes.copyOfRange(index, bytes.size)
        }
    }

    private var cachedKeyPair: KeyPair? = null
    private var cachedCertificate: X509Certificate? = null

    init {
        loadOrGenerateCertificate()
    }

    fun getClientCertificate(): X509Certificate =
        cachedCertificate ?: loadOrGenerateCertificate().second

    fun getClientPublicKey(): RSAPublicKey = getClientCertificate().publicKey as RSAPublicKey

    fun clearPairing(): Boolean {
        cachedKeyPair = null
        cachedCertificate = null
        return clearAllPairingData(context)
    }

    @Synchronized
    private fun loadOrGenerateCertificate(): Pair<KeyPair, X509Certificate> {
        cachedKeyPair?.let { pair ->
            cachedCertificate?.let { cert ->
                return Pair(pair, cert)
            }
        }

        val keystoreFile = File(context.filesDir, KEYSTORE_NAME)
        val keyStore = KeyStore.getInstance("PKCS12")

        if (keystoreFile.exists()) {
            try {
                FileInputStream(keystoreFile).use { fis ->
                    keyStore.load(fis, KEYSTORE_PASSWORD)
                }
                val privateKey =
                    keyStore.getKey(KEY_ALIAS, KEYSTORE_PASSWORD) as? RSAPrivateKey
                val cert = keyStore.getCertificate(KEY_ALIAS) as? X509Certificate
                if (privateKey != null && cert != null) {
                    val keyPair = KeyPair(cert.publicKey, privateKey)
                    cachedKeyPair = keyPair
                    cachedCertificate = cert
                    return Pair(keyPair, cert)
                }
            } catch (e: Exception) {
                keystoreFile.delete()
            }
        }

        // Generate new RSA-2048 keypair
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        val keyPair = keyGen.generateKeyPair()

        // Generate self-signed X.509 certificate
        val now = System.currentTimeMillis()
        val notBefore = Date(now - 24 * 60 * 60 * 1000L)
        val notAfter = Date(now + 25L * 365 * 24 * 60 * 60 * 1000L)
        val subject = X500Name("CN=Nope Remote")
        val serialNumber = BigInteger.valueOf(Math.abs(now))

        val certBuilder = JcaX509v3CertificateBuilder(
            subject,
            serialNumber,
            notBefore,
            notAfter,
            subject,
            keyPair.public,
        )

        certBuilder.addExtension(
            Extension.basicConstraints,
            false,
            BasicConstraints(false),
        )
        certBuilder.addExtension(
            Extension.keyUsage,
            true,
            KeyUsage(
                KeyUsage.digitalSignature or
                    KeyUsage.keyEncipherment or
                    KeyUsage.keyCertSign,
            ),
        )
        certBuilder.addExtension(
            Extension.subjectAlternativeName,
            false,
            GeneralNames(GeneralName(GeneralName.dNSName, "atvremote")),
        )

        val signer = JcaContentSignerBuilder("SHA256withRSA").build(keyPair.private)
        val cert = JcaX509CertificateConverter().getCertificate(certBuilder.build(signer))

        // Save to PKCS12 KeyStore
        keyStore.load(null, KEYSTORE_PASSWORD)
        keyStore.setKeyEntry(
            KEY_ALIAS,
            keyPair.private,
            KEYSTORE_PASSWORD,
            arrayOf<Certificate>(cert),
        )

        FileOutputStream(keystoreFile).use { fos ->
            keyStore.store(fos, KEYSTORE_PASSWORD)
        }

        cachedKeyPair = keyPair
        cachedCertificate = cert
        return Pair(keyPair, cert)
    }

    fun getKeyManagers(): Array<KeyManager> {
        val keystoreFile = File(context.filesDir, KEYSTORE_NAME)
        val keyStore = KeyStore.getInstance("PKCS12")
        FileInputStream(keystoreFile).use { fis ->
            keyStore.load(fis, KEYSTORE_PASSWORD)
        }
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, KEYSTORE_PASSWORD)
        return kmf.keyManagers
    }

    fun getTrustManagers(): Array<TrustManager> = arrayOf(
        object : X509TrustManager {
            override fun checkClientTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?,
            ) = Unit

            override fun checkServerTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?,
            ) = Unit

            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        },
    )

    fun createSslContext(): SSLContext {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(getKeyManagers(), getTrustManagers(), SecureRandom())
        try {
            sslContext.clientSessionContext.sessionCacheSize = 0
            sslContext.clientSessionContext.sessionTimeout = 0
        } catch (_: Exception) {}
        return sslContext
    }
}
