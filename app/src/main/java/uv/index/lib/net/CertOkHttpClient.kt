package uv.index.lib.net

import android.annotation.SuppressLint
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import okhttp3.OkHttpClient
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*


class CertOkHttpClient(
    @PublishedApi internal val config: HttpClientConfig<OkHttpConfig>
) {

    @PublishedApi
    internal val client by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        createClient(config)
    }

    @PublishedApi
    @Volatile
    internal var isCertPathValidatorException: Boolean = false

    @PublishedApi
    internal val certClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        createClient(config, getUnsafeOkHttpClient())
    }
    fun getClient(): HttpClient {
        while (true) {
            try {
                return if (isCertPathValidatorException) {
                    certClient
                } else {
                    client
                }
            } catch (e: SSLException) {
                if (!isCertPathValidatorException) isCertPathValidatorException = true else throw e
            }
        }
    }

    suspend inline fun <reified T> get(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        return getClient().get(urlString, block).body()
//        while (true) {
//            try {
//                return if (isCertPathValidatorException) {
//                    certClient.get(urlString, block).body()
//                } else {
//                    client.get(urlString, block).body()
//                }
//            } catch (e: SSLException) {
//                if (!isCertPathValidatorException) isCertPathValidatorException = true else throw e
//            }
//        }
    }

    companion object {

        @PublishedApi
        internal fun createClient(
            config: HttpClientConfig<OkHttpConfig>,
            preconfiguredClient: OkHttpClient? = null
        ): HttpClient {
            return HttpClient(OkHttp) {
                plusAssign(config)
                if (preconfiguredClient != null) {
                    engine {
                        preconfigured = preconfiguredClient
                    }
                }
            }
        }

        // https://stackoverflow.com/questions/6825226/trust-anchor-not-found-for-android-ssl-connection
        @PublishedApi
        internal fun getUnsafeOkHttpClient(): OkHttpClient {
            val trustAllCerts = arrayOf<TrustManager>(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {

                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) = Unit

                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?
                    ) = Unit

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(
                null, trustAllCerts,
                java.security.SecureRandom()
            )

            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }

            return builder.build()
        }
    }
}