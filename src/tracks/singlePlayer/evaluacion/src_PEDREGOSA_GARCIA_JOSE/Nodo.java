package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import static tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Constantes.*;
import tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Mapa;
import ontology.Types.ACTIONS;
import java.util.ArrayList;
import java.util.Objects;


public class Nodo {
    private static final short[] dx = {1, 0, -1, 0};
    private static final short[] dy = {0, -1, 0, 1};
    private static final ACTIONS[] acciones = {ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_UP, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_DOWN};

    public short x;
    public short y;

    private ACTIONS accionPrecedente; // Acción que se tomó para llegar a este nodo desde su padre
    private Nodo padre;
    // flags:
    // [0]    [0 0 0]             [0]      [0 0 0]
    // Libre  Direccion de vuelo  Llave    Moneda
    // Bit 7  Bits 4-6            Bit 3    Bits 0-2
    // 0 no volando 1 norte 2 sur 3 este 4 oeste
    private byte flags;

    // bitset para marcar si se han recogido las monedas
    public long monedas1 = 0L;
    public long monedas2 = 0L;
    // bitset para marcar si se han activado las catapultas
    public long catapultas1 = 0L;
    public long catapultas2 = 0L;

    public Nodo(short x, short y, int moneda, boolean llave, byte volando) {
        this.x = x;
        this.y = y;
        this.flags = 0;
        
        setMoneda(moneda);
        setLlave(llave);
        setVolando(volando);
        this.padre = null;
    }

    public Nodo(short x, short y, Nodo padre) {
        this.x = x;
        this.y = y;
        this.flags = padre.flags;
        this.monedas1 = padre.monedas1;
        this.monedas2 = padre.monedas2;
        this.catapultas1 = padre.catapultas1;
        this.catapultas2 = padre.catapultas2;
        this.padre = padre;
    }

    // TODO probar que funciona correctamente
    public ArrayList<Nodo> expandir(Mapa mapa) {
        ArrayList<Nodo> hijos = new ArrayList<>();
        if (estaMuerto(mapa))
            return hijos;
        // Movimiento volando
        byte dirVuelo = isVolando();
        if (dirVuelo != 0) {
            Nodo hijo = new Nodo(x, y, this);
            hijo.accionPrecedente = ACTIONS.ACTION_NIL;

            int vx = 0, vy = 0;
            if (dirVuelo == DIREAST) vx = 1;
            else if (dirVuelo == DIRNORTH) vy = -1;
            else if (dirVuelo == DIRWEST) vx = -1;
            else if (dirVuelo == DIRSOUTH) vy = 1;

            short newX = (short) (x + vx);
            short newY = (short) (y + vy);
            if (outOfBounds(newX, newY, mapa) || mapa.grid[newY][newX] == PARED) {
                hijo.setVolando(0);
                if (!hijo.estaMuerto(mapa))
                    hijos.add(hijo);
                return hijos;
            }
            hijo.x = newX;
            hijo.y = newY;

            int nuevaCatapulta = mapa.mapaCatapultas[newY][newX];
            if (nuevaCatapulta != -1) {
                int idCat = mapa.idCatapultas[newY][newX];
                if (!hijo.isCatapultaActivada(idCat)) {
                    hijo.setVolandoCat(nuevaCatapulta);
                    hijo.marcarCatapultaActivada(idCat);
                }
            }

            if (mapa.mapaLlaves[newY][newX] && !hijo.hasLlave())
                hijo.setLlave(true);

            if (mapa.mapaMonedas[newY][newX]) {
                int idMoneda = mapa.idMonedas[newY][newX];
                if (!hijo.isMonedaRecogida(idMoneda) && hijo.masMoneda())
                    hijo.marcarMonedaRecogida(idMoneda);
            }

            hijos.add(hijo);
            return hijos;
        }

        byte tipoCatapultaActual = mapa.mapaCatapultas[y][x];
        if (tipoCatapultaActual != -1) {
            int idCat = mapa.idCatapultas[y][x];
            if (!isCatapultaActivada(idCat)) {
                Nodo hijo = new Nodo(x, y, this);
                hijo.accionPrecedente = ACTIONS.ACTION_NIL;
                hijo.setVolandoCat(tipoCatapultaActual);
                hijo.marcarCatapultaActivada(idCat);
                hijo.menosMoneda(); // Pierde una moneda al usar la catapulta
                if (!hijo.estaMuerto(mapa))
                    hijos.add(hijo);
                return hijos;
            }
        }

        // Movimiento no volando
        for (int i = 0; i < 4; i++) {
            short newX = (short) (x + dx[i]);
            short newY = (short) (y + dy[i]);
            if (outOfBounds(newX, newY, mapa)) continue;
            if ((mapa.grid[newY][newX] == PARED) ||
                (mapa.grid[newY][newX] == AGUA) || 
                (mapa.grid[newY][newX] == PUERTA && !hasLlave())) {
                continue; // No se puede pasar por paredes ni por agua si no se está volando
            }
            byte proximaCatapulta = mapa.mapaCatapultas[newY][newX];
            if (proximaCatapulta != -1 && getMoneda() == 0) {
                int idCat = mapa.idCatapultas[newY][newX];
                if (!isCatapultaActivada(idCat))
                    continue; // No se puede entrar en una casilla con catapulta si no se tienen monedas para usarla
            }
            
            Nodo hijo = new Nodo(newX, newY, this);
            hijo.accionPrecedente = acciones[i];
            if (mapa.mapaMonedas[newY][newX] && getMoneda() < 5) {
                int idMoneda = mapa.idMonedas[newY][newX];
                if (!hijo.isMonedaRecogida(idMoneda) && hijo.masMoneda())
                    hijo.marcarMonedaRecogida(idMoneda);
            }
            if (mapa.mapaLlaves[newY][newX] && !hijo.hasLlave())
                hijo.setLlave(true);
            hijos.add(hijo);
        }
        return hijos;
    }

    // --- MÉTODOS DE ACCESO A BITS ---

    public int getMoneda() {
        // (00000111) para leer los 3 primeros bits
        return flags & 0x07; 
    }

    protected void setMoneda(int moneda) {
        // Limpiamos los 3 primeros bits y escribimos el nuevo valor (0-5)
        if (moneda < 0) moneda = 7; // Valor 7 significa muerto (ha perdido mas monedas de las que tenía o ha caido al agua)
        flags = (byte) ((flags & ~0x07) | (moneda & 0x07));
    }

    public boolean masMoneda() {
        int moneda = getMoneda();
        if (moneda < 5) {
            setMoneda(moneda + 1);
            return true;
        }
        return false; // No se puede recoger más monedas
    }

    public void menosMoneda() {
        setMoneda(getMoneda() - 1);
    }

    public boolean hasLlave() {
        // (00001000 en binario) para leer el 4to bit
        return (flags & 0x08) != 0; 
    }

    public void setLlave(boolean llave) {
        if (llave) flags |= 0x08;      // Enciende el bit
        else       flags &= ~0x08;     // Apaga el bit
    }

    public byte isVolando() {
        return (byte) ((flags >> 4) & 0x07); // (01110000) para leer los bits 4-6 
    }

    public void setVolando(int direccion) {
        if (direccion < 0 || direccion > 4) {
            direccion = 0; // No volando
        }
        // 1. Limpiamos los bits 4, 5 y 6 usando la máscara 10001111 (0x8F)
        // 2. Desplazamos la nueva dirección 4 posiciones a la izquierda
        // 3. Unimos todo con un OR bit a bit (|)
        flags = (byte) ((flags & 0x8F) | ((direccion & 0x07) << 4));
    }

    public void setVolandoCat(int tipoCatapulta) {
        if (tipoCatapulta == CATSOUTH) setVolando(DIRSOUTH);
        else if (tipoCatapulta == CATNORTH) setVolando(DIRNORTH);
        else if (tipoCatapulta == CATEAST) setVolando(DIREAST);
        else if (tipoCatapulta == CATWEST) setVolando(DIRWEST);
    }

    private boolean isMonedaRecogida(int id) {
        if (id < 0 || id >= 128) return true; // ID fuera de rango
        int bloque = id / 64;
        int bit = id & 63; // id % 64
        if (bloque == 0)
            return (monedas1 & (1L << bit)) != 0;
        else
            return (monedas2 & (1L << bit)) != 0;
    }

    private void marcarMonedaRecogida(int id) {
        if (id < 0 || id >= 128) return;
        int bloque = id / 64;
        int bit = id & 63; // id % 64
        if (bloque == 0)
            monedas1 |= (1L << bit);
        else
            monedas2 |= (1L << bit);
    }

     private boolean isCatapultaActivada(int id) {
        if (id < 0 || id >= 128) return true; // ID fuera de rango
        int bloque = id / 64;
        int bit = id & 63; // id % 64
        if (bloque == 0)
            return (catapultas1 & (1L << bit)) != 0;
        else
            return (catapultas2 & (1L << bit)) != 0;
    }

    private void marcarCatapultaActivada(int id) {
        if (id < 0 || id >= 128) return;
        int bloque = id / 64;
        int bit = id & 63; // id % 64
        if (bloque == 0)
            catapultas1 |= (1L << bit);
        else
            catapultas2 |= (1L << bit);
    }

    private boolean outOfBounds(int x, int y, Mapa mapa) {
        return x < 0 || x >= mapa.xmax || y < 0 || y >= mapa.ymax;
    }
    

    // --- OPERACIONES ---

    public int distanciaManhattan(Nodo otro) {
        return Math.abs(this.x - otro.x) + Math.abs(this.y - otro.y);
    }

    public boolean estaMuerto(Mapa mapa) {
        if (isVolando() == 0 && getCasilla(mapa) == AGUA)
            return true;
        return getMoneda() == 7;
    }

    public int getCasilla(Mapa mapa) {
        return mapa.grid[y][x];
    }

    public ACTIONS getAccionPadre() {
        return accionPrecedente;
    }

    public Nodo getPadre() {
        return padre;
    }

    @Override
    public String toString() {
        String nodo = String.format("Nodo(x=%d, y=%d, moneda=%d, llave=%b, volando=%d, AccionPadre=%s)", 
            x, y, getMoneda(), hasLlave(), isVolando(), accionPrecedente);
        return nodo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Nodo otro = (Nodo) obj;
        return this.x == otro.x && 
               this.y == otro.y &&
               this.flags == otro.flags &&
               this.monedas1 == otro.monedas1 &&
               this.monedas2 == otro.monedas2 &&
               this.catapultas1 == otro.catapultas1 &&
               this.catapultas2 == otro.catapultas2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, flags, monedas1, monedas2, catapultas1, catapultas2);
    }

}