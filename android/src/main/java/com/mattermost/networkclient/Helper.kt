package com.mattermost.networkclient

import com.facebook.react.bridge.*
import okhttp3.*
import java.util.concurrent.TimeUnit
import com.mattermost.networkclient.interceptors.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject


/**
 * Parses the response data into the format expected by the App
 *
 * @return WriteableMap for passing back to App
 */
fun Response.returnAsWriteableMap(): WritableMap {
    val headers = Arguments.createMap();
    this.headers.forEach { k -> headers.putString(k.first, k.second) }

    val map = Arguments.createMap()
    map.putMap("headers", headers)
    map.putString("data", this.body!!.string())
    map.putInt("code", this.code)
    map.putBoolean("ok", this.isSuccessful)
    map.putString("lastRequestedUrl", this.request.url.toString())
    return map;
}

/**
 * Completes the response handling by calling a promise resolve / reject based on response type
 *
 * @param promise The promise to resolve/reject
 */
fun Response.promiseResolution(promise: Promise): Response {
    if (this.isSuccessful) {
        promise.resolve(this.returnAsWriteableMap());
    } else {
        promise.reject(this.code.toString(), this.returnAsWriteableMap())
    }
    return this
}

/**
 * Parses options passed in over the bridge for individual requests
 *
 * @param options ReadableMap of options from the App
 */
fun Request.Builder.parseOptions(options: ReadableMap, session: OkHttpClient.Builder): Request.Builder {

    // Timeout Interval per request
    if (options.hasKey("timeoutInterval")) {
        session.addInterceptor(TimeoutRequestInterceptor(options.getInt("timeoutInterval")))
    }

    // Headers
    if (options.hasKey("headers")) {
        this.addReadableMap(options.getMap("headers")!!)
    }

    // Need to always close the connection once finished
    this.header("Connection", "close")

    return this;
}

/**
 * Parses options passed in over the bridge for client sessions
 *
 * @params options ReadableMap of options from the App
 */
fun OkHttpClient.Builder.parseOptions(options: ReadableMap, request: Request.Builder?): OkHttpClient.Builder {
    // Following Redirects
    if (options.hasKey("followRedirects")) {
        val followRedirects = options.getBoolean("followRedirects")
        this.followRedirects(followRedirects)
        this.followSslRedirects(followRedirects)
    }

    // Retries
    this.retryOnConnectionFailure(false);
    if (options.hasKey("retryPolicyConfiguration")) {
        val retryPolicyConfiguration = options.getMap("retryPolicyConfiguration")!!.toHashMap();

        val retryType = retryPolicyConfiguration["type"] as String?
        val retryLimit = retryPolicyConfiguration["retryLimit"] as Double?
        val retryExponentialBackoffBase = retryPolicyConfiguration["exponentialBackoffBase"] as Double?
        val retryExponentialBackoffScale = retryPolicyConfiguration["exponentialBackoffScale"] as Double?
        this.addInterceptor(RetryInterceptor(retryType, retryLimit?.toInt(), retryExponentialBackoffBase, retryExponentialBackoffScale))
    }

    // Headers
    if (options.hasKey("headers")) {
        request?.addReadableMap(options.getMap("headers")!!)
    }

    // Session Configuration
    if (options.hasKey("sessionConfiguration")) {
        val sessionConfig = options.getMap("sessionConfiguration")!!;

        if (sessionConfig.hasKey("followRedirects")) {
            val followRedirects = sessionConfig.getBoolean("followRedirects")
            this.followRedirects(followRedirects);
            this.followSslRedirects(followRedirects);
        }

        if (sessionConfig.hasKey("timeoutIntervalForRequest")) {
            this.connectTimeout(sessionConfig.getInt("timeoutIntervalForRequest").toLong(), TimeUnit.SECONDS)
            this.readTimeout(sessionConfig.getInt("timeoutIntervalForRequest").toLong(), TimeUnit.SECONDS)
        }

        if (sessionConfig.hasKey("timeoutIntervalForResource")) {
            this.callTimeout(sessionConfig.getInt("timeoutIntervalForResource").toLong(), TimeUnit.SECONDS)
        }

        if (sessionConfig.hasKey("httpMaximumConnectionsPerHost")) {
            val maxConnections = sessionConfig.getInt("httpMaximumConnectionsPerHost");
            val dispatcher = Dispatcher()
            dispatcher.maxRequests = maxConnections
            dispatcher.maxRequestsPerHost = maxConnections
            this.dispatcher(dispatcher);
        }

        // WS: Timeout
        if (sessionConfig.hasKey("timeoutInterval")) {
            this.connectTimeout(sessionConfig.getInt("timeoutInterval").toLong(), TimeUnit.SECONDS)
            this.readTimeout(sessionConfig.getInt("timeoutInterval").toLong(), TimeUnit.SECONDS)
            this.callTimeout(sessionConfig.getInt("timeoutInterval").toLong(), TimeUnit.SECONDS)
        }

        // WS: Compression
        if (sessionConfig.hasKey("enableCompression")) {
            this.minWebSocketMessageToCompress(0);
        }
    }

    return this
}

/**
 * Parses the header data into the format expected by the App
 *
 * @return WriteableMap for passing back to App
 */
fun Headers.readableMap(): ReadableMap {
    val headersMap = Arguments.createMap()
    this.forEach { (k, v) -> headersMap.putString(k, v) }
    return headersMap
}

/**
 * Adds the headers object passed in by the app to the request
 *
 * @options headers ReadableMap
 */
fun Request.Builder.addReadableMap(headers: ReadableMap): Request.Builder {
    for ((k, v) in headers.toHashMap()) {
        this.removeHeader(k);
        this.addHeader(k, v as String);
    }
    return this
}

/**
 * Transforms the "body" for a POST/PATCH/PUT/DELETE request to a Request Body
 *
 * @return RequestBody
 */
fun ReadableMap.bodyToRequestBody(): RequestBody {
    if (!this.hasKey("body")) return "".toRequestBody()
    return if (this.getType("body") === ReadableType.Map) {
        JSONObject(this.getMap("body")!!.toHashMap()).toString().toRequestBody()
    } else {
        this.getString("body")!!.toRequestBody()
    }
}

/**
 * Forms a URL String from the given params
 *
 * @param baseUrl
 * @param endpoint
 * @return url string
 */
fun formUrlString(baseUrl: String, endpoint: String): String{
    return baseUrl.toHttpUrlOrNull()!!.newBuilder().addPathSegments(endpoint.trim { c -> c == '/' }).build().toString()
}