package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

public class Nodo {
    // BORRAR SOLO ES PLACEHOLDER
    public static final int AGUA = 1;

    // 1. Puntero al padre (referencia, ocupa 4 u 8 bytes dependiendo de la JVM)
    public Nodo padre;

    // 2. Coordenadas usando short (2 bytes cada uno)
    public short x;
    public short y;
    
    // 3. Empaquetado de bits manual en un solo byte (1 byte)
    // Layout del byte 'flags':
    // [0 0 0]   [0]      [0]     [0 0 0]
    // Libres   Volando  Llave    Moneda
    // Bits 5-7  Bit 4    Bit 3   Bits 0-2
    private byte flags;

    public Nodo(short x, short y, Nodo padre, int moneda, boolean llave, boolean volando) {
        this.x = x;
        this.y = y;
        this.padre = padre;
        this.flags = 0; // Inicializar a 0
        
        setMoneda(moneda);
        setLlave(llave);
        setVolando(volando);
    }

    // --- MÉTODOS DE ACCESO A BITS ---

    public int getMoneda() {
        // Máscara 0x07 (00000111 en binario) para leer los 3 primeros bits
        return flags & 0x07; 
    }

    public void setMoneda(int moneda) {
        // Limpiamos los 3 primeros bits y escribimos el nuevo valor (0-5)
        if (moneda > 5) moneda = 5; // Limitar a 5 monedas
        if (moneda < 0) moneda = 7; // Valor 7 significa muerto (ha perdido mas monedas de las que tenía o ha caido al agua)
        flags = (byte) ((flags & ~0x07) | (moneda & 0x07));
    }

    public boolean hasLlave() {
        // Máscara 0x08 (00001000 en binario) para leer el 4to bit
        return (flags & 0x08) != 0; 
    }

    public void setLlave(boolean llave) {
        if (llave) flags |= 0x08;      // Enciende el bit
        else       flags &= ~0x08;     // Apaga el bit
    }

    public boolean isVolando() {
        // Máscara 0x10 (00010000 en binario) para leer el 5to bit
        return (flags & 0x10) != 0; 
    }

    public void setVolando(boolean volando) {
        if (volando) flags |= 0x10;    // Enciende el bit
        else         flags &= ~0x10;   // Apaga el bit
    }

    // --- OPERACIONES ---

    // Distancia Manhattan
    public int distanciaManhattan(Nodo otro) {
        return Math.abs(this.x - otro.x) + Math.abs(this.y - otro.y);
    }

    public boolean estaMuerto() {
        if (!isVolando() && getCasilla() == AGUA)
            return true;
        return getMoneda() == 7;
    }

    public int getCasilla(Mapa mapa) {
        // Consiguir el estado de la casilla actual con el framework de GVGAI
        return 0; // Placeholder
    }
}