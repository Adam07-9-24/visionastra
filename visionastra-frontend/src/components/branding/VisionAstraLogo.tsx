export default function VisionAstraLogo({
  size = 35,
  bgOpacity = 0.12,
  showText = true,
}: {
  size?: number;
  bgOpacity?: number;
  showText?: boolean;
}) {
  return (
    <div className="flex items-center gap-3">
      <svg
        width={size}
        height={size}
        viewBox="0 0 44 44"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="shrink-0 text-primary"
      >
        {/* FONDO DINÁMICO */}
        <rect
          width="44"
          height="44"
          rx="11"
          fill="currentColor"
          opacity={bgOpacity}
        />

        {/* OVERLAY SUAVE */}
        <rect width="44" height="44" rx="11" fill="white" fillOpacity="0.04" />

        {/* ÓRBITA TRASERA (PULSO) */}
        <ellipse
          cx="22"
          cy="22"
          rx="18.5"
          ry="10.5"
          transform="rotate(-25 22 22)"
          stroke="currentColor"
          strokeWidth="1"
          strokeDasharray="2 3"
          fill="none"
          style={{ animation: "va-pulse 3s ease-in-out infinite" }}
        />

        {/* CHIP */}
        <rect
          x="13.5"
          y="13.5"
          width="17"
          height="17"
          rx="7"
          stroke="currentColor"
          strokeWidth="1.4"
          fill="currentColor"
          opacity="0.05"
        />

        {/* BARRAS (MEJOR VISUAL) */}
        {/* BARRAS ESTILO ESTADÍSTICA REAL */}
        <rect
          x="17.2"
          y="26.5"
          width="2.2"
          height="3"
          rx="0.4"
          fill="currentColor"
          opacity="0.7"
        />

        <rect
          x="20.4"
          y="24"
          width="2.2"
          height="5.5"
          rx="0.4"
          fill="currentColor"
          opacity="0.85"
        />

        <rect
          x="23.6"
          y="21.5"
          width="2.2"
          height="8"
          rx="0.4"
          fill="currentColor"
        />

        {/* ÓRBITA DELANTERA */}
        <ellipse
          cx="22"
          cy="22"
          rx="18.5"
          ry="10.5"
          transform="rotate(-25 22 22)"
          stroke="currentColor"
          strokeWidth="1.5"
          fill="none"
          strokeLinecap="round"
          strokeDasharray="36 6"
        />

        {/* ESTRELLA */}
        {/* ESTRELLA MEJORADA */}
        <g
          style={{
            transformOrigin: "36px 9px",
            animation: "va-spin 6s ease-in-out infinite",
          }}
        >
          {/* BRILLO */}
          <path
            d="M36 6l1.2 3 3 1.2-3 1.2-1.2 3-1.2-3-3-1.2 3-1.2L36 6z"
            fill="currentColor"
            opacity="0.35"
          />

          {/* NÚCLEO */}
          <path
            d="M36 7.5l0.8 2 2 0.8-2 0.8-0.8 2-0.8-2-2-0.8 2-0.8 0.8-2z"
            fill="currentColor"
          />
        </g>

        {/* ANIMACIONES */}
        <style>{`
          @keyframes va-spin {
            0%,100% { transform: rotate(0deg) scale(1); }
            50% { transform: rotate(180deg) scale(1.15); }
          }
          @keyframes va-pulse {
            0%,100% { opacity: 0.2; }
            50% { opacity: 0.55; }
          }
        `}</style>
      </svg>

      {showText && (
        <div className="flex flex-col gap-[3px]">
          <span className="text-[17px] font-semibold tracking-[-0.01em] text-foreground leading-none">
            VisionAstra
          </span>

          <span className="text-[12px] tracking-[0.08em] text-foreground/60">
            Impulso con IA
          </span>
        </div>
      )}
    </div>
  );
}
