import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private isConnected = false;

  public connect(onConnected?: () => void, onError?: (err: any) => void) {
    if (this.client && this.client.active) {
      if (this.isConnected && onConnected) onConnected();
      return;
    }

    const wsUrl = import.meta.env.VITE_WS_BASE_URL || '/ws';

    this.client = new Client({
      webSocketFactory: () => new SockJS(wsUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: (_msg) => {
        // debug logging if needed
      },
      onConnect: () => {
        this.isConnected = true;
        if (onConnected) onConnected();
      },
      onDisconnect: () => {
        this.isConnected = false;
      },
      onStompError: (frame) => {
        console.error('STOMP Broker error', frame.headers['message']);
        if (onError) onError(frame);
      },
    });

    this.client.activate();
  }

  public subscribe(topic: string, callback: (data: any) => void): () => void {
    if (!this.client || !this.client.connected) {
      this.connect(() => {
        this.doSubscribe(topic, callback);
      });
      return () => this.unsubscribe(topic);
    }

    this.doSubscribe(topic, callback);
    return () => this.unsubscribe(topic);
  }

  private doSubscribe(topic: string, callback: (data: any) => void): void {
    if (this.subscriptions.has(topic)) {
      this.subscriptions.get(topic)?.unsubscribe();
    }

    if (!this.client || !this.client.connected) return;

    const sub = this.client.subscribe(topic, (message: IMessage) => {
      try {
        const payload = JSON.parse(message.body);
        callback(payload);
      } catch {
        callback(message.body);
      }
    });

    this.subscriptions.set(topic, sub);
  }

  public unsubscribe(topic: string) {
    const sub = this.subscriptions.get(topic);
    if (sub) {
      sub.unsubscribe();
      this.subscriptions.delete(topic);
    }
  }

  public disconnect() {
    if (this.client) {
      this.subscriptions.forEach((sub) => sub.unsubscribe());
      this.subscriptions.clear();
      this.client.deactivate();
      this.isConnected = false;
      this.client = null;
    }
  }
}

export const websocketService = new WebSocketService();
export default websocketService;
