// Copyright (c) 2015-present Mattermost, Inc. All Rights Reserved.
// See LICENSE.txt for license information.

type Constants = {
    EXPONENTIAL_RETRY: "EXPONENTIAL_RETRY";
};

interface NativeAPIClient {
    get(
        baseUrl: string,
        endpoint: string | null,
        options?: RequestOptions
    ): Promise<ClientResponse>;
    put(
        baseUrl: string,
        endpoint: string | null,
        options?: RequestOptions
    ): Promise<ClientResponse>;
    post(
        baseUrl: string,
        endpoint: string | null,
        options?: RequestOptions
    ): Promise<ClientResponse>;
    patch(
        baseUrl: string,
        endpoint: string | null,
        options?: RequestOptions
    ): Promise<ClientResponse>;
    delete(
        baseUrl: string,
        endpoint: string | null,
        options?: RequestOptions
    ): Promise<ClientResponse>;
    upload(
        baseUrl: string,
        endpoint: string | null,
        fileUrl: string,
        options?: UploadRequestOptions
    ): Promise<ClientResponse>;

    createClientFor(
        baseUrl: string,
        config?: APIClientConfiguration
    ): Promise<void>;

    getClientHeadersFor(baseUrl: string): Promise<ClientHeaders>;
    addClientHeadersFor(baseUrl: string, headers: ClientHeaders): Promise<void>;
    invalidateClientFor(baseUrl: string): Promise<void>;

    createWebSocketClientFor(
        wsUrl: string,
        callbacks: WebSocketCallbacks,
        config?: WebSocketClientConfiguration
    ): Promise<void>;
    disconnectWebSocketFor(wsUrl: string): Promise<void>;
    invalidateWebSocketClientFor(baseUrl: string): Promise<void>;

    getConstants(): Constants;
}
