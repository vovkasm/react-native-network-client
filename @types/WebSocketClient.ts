// Copyright (c) 2015-present Mattermost, Inc. All Rights Reserved.
// See LICENSE.txt for license information.

type SSLPinningConfiguration = {
    enabled: boolean;
    allowSelfSigned?: boolean;
};

type WebSocketClientConfiguration = {
    headers?: ClientHeaders;
    timeoutInterval?: number;
    enableCompression?: boolean;
    sslPinningConfiguration?: SSLPinningConfiguration;
};

type WebSocketMessage = string | Record<string, string | number>;
type WebSocketEvent = {
    url: string;
    message: WebSocketMessage;
};

type WebSocketCallback = (event: WebSocketEvent) => void;

interface WebSocketClientInterface {
    url: string;
    readyState: number;
    send(data: string): void;
    connect(): void;
    close(): void;
    onOpen(callback: WebSocketCallback): void;
    onClose(callback: WebSocketCallback): void;
    onError(callback: WebSocketCallback): void;
    onMessage(callback: WebSocketCallback): void;
    onOpenCallback?: WebSocketCallback;
    onCloseCallback?: WebSocketCallback;
}
