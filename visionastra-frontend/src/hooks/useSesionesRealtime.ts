import { useEffect } from "react";
import { Client } from "@stomp/stompjs";

type SesionEvento = {
  tipo: string;
  mensaje: string;
  idSesion: number;
  idUsuario: number;
};

type UseSesionesRealtimeOptions = {
  enabled?: boolean;
  onEvento: (evento: SesionEvento) => void;
};

export function useSesionesRealtime({
  enabled = true,
  onEvento,
}: UseSesionesRealtimeOptions) {
  useEffect(() => {
    if (!enabled) {
      return;
    }

    const client = new Client({
      brokerURL: "ws://localhost:8083/ws",
      reconnectDelay: 5000,

      debug: () => {},

      beforeConnect: () => {
        const token = localStorage.getItem("token");

        if (!token) {
          throw new Error("No hay token para conectar WebSocket");
        }

        client.connectHeaders = {
          Authorization: `Bearer ${token}`,
        };
      },

      onConnect: () => {
        client.subscribe("/user/queue/sesiones", (message) => {
          try {
            const evento = JSON.parse(message.body) as SesionEvento;
            onEvento(evento);
          } catch {
            onEvento({
              tipo: "SESIONES_ACTUALIZADAS",
              mensaje: "Las sesiones fueron actualizadas",
              idSesion: 0,
              idUsuario: 0,
            });
          }
        });
      },

      onStompError: () => {},

      onWebSocketError: () => {},

      onWebSocketClose: () => {},
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [enabled, onEvento]);
}
