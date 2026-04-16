package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import static tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Constantes.*;
import tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Mapa;
import ontology.Types.ACTIONS;
import java.util.ArrayList;
import java.util.Objects;


public class Nodo implements Comparable<Nodo> {
	private static final short[] dx = {1, 0, -1, 0};
	private static final short[] dy = {0, -1, 0, 1};
	private static final ACTIONS[] acciones = {ACTIONS.ACTION_RIGHT, ACTIONS.ACTION_UP, ACTIONS.ACTION_LEFT, ACTIONS.ACTION_DOWN};

	public short x;
	public short y;

	private ACTIONS accionPrecedente; // Acción que se tomó para llegar a este nodo desde su padre
	private Nodo padre;
	// flags:
	// [0]	[0 0 0]			 [0]	  [0 0 0]
	// Libre  Direccion de vuelo  Llave	Moneda
	// Bit 7  Bits 4-6			Bit 3	Bits 0-2
	// 0 no volando 1 norte 2 sur 3 este 4 oeste
	private byte flags;

	// bitset para marcar si se han recogido las monedas
	public long monedas1 = 0L;
	public long monedas2 = 0L;
	// bitset para marcar si se han activado las catapultas
	public long catapultas1 = 0L;
	public long catapultas2 = 0L;

	// Para algoritmos informados
	public int g = 0; // coste desde el nodo inicial hasta este nodo
	public int h = 0; // heurística (estimación del coste desde este nodo hasta el objetivo)
	public int f = 0; // f = g + h
	public int idInsercion; // para el desempate en la cola de prioridad (nodo más antiguo)

	/**
	 * Constructor para el nodo raíz.
	 * @param x
	 * @param y
	 * @param moneda
	 * @param llave
	 * @param volando dirección de vuelo (0 para no volando, 1 norte, 2 sur, 3 este, 4 oeste)
	 */
	public Nodo(short x, short y, int moneda, boolean llave, byte volando) {
		this.x = x;
		this.y = y;
		this.flags = 0;
		
		setMoneda(moneda);
		setLlave(llave);
		setVolando(volando);
		this.padre = null;
	}

	/**
	 * Copia el estado del nodo padre y lo actualiza con la nueva posición (x,y).
	 * @param x
	 * @param y
	 * @param padre
	 */
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

	/**
	 * Genera los nodos hijos del nodo actual, aplicando las reglas del juego 
	 * (movimiento, recogida de monedas, uso de catapultas, etc.) según el mapa dado
	 * @param mapa
	 * @return
	 */
	public ArrayList<Nodo> expandir(Mapa mapa) {
		ArrayList<Nodo> hijos = new ArrayList<>();
		// Movimiento volando
		byte dirVuelo = isVolando();
		if (dirVuelo != 0) {
			Nodo hijo = new Nodo(x, y, this);
			hijo.accionPrecedente = ACTIONS.ACTION_NIL;
			
			// Nos encontramos una catapulta en el camino,
			// se tiene que activar, cambiando la dirección de vuelo,
			// accionPrecedente ACTION_NIL y tenemos que hacer ACTION_NIL en el siguiente paso
			int nuevaCatapulta = mapa.mapaCatapultas[y][x];
			int idNuevaCatapulta = mapa.idCatapultas[y][x];
			if (nuevaCatapulta != -1 && !hijo.isCatapultaActivada(idNuevaCatapulta)) {
				hijo.setVolandoCat(nuevaCatapulta);
				hijo.marcarCatapultaActivada(idNuevaCatapulta);
				hijos.add(hijo);
				return hijos;
			}

			short newX = (short) (x + dx[dirVuelo - 1]);
			short newY = (short) (y + dy[dirVuelo - 1]);

			// Mirar si nos hemos chocado con una pared o puerta cerrada
			if (outOfBounds(newX, newY, mapa) || mapa.grid[newY][newX] == PARED ||
				(mapa.grid[newY][newX] == PUERTA && !hasLlave())) {
				hijo.setVolando(0);
				if (!hijo.estaMuerto(mapa))
					hijos.add(hijo);
				return hijos;
			}

			hijo.x = newX;
			hijo.y = newY;
			
			
			// Recoger llave
			if (mapa.mapaLlaves[newY][newX] && !hijo.hasLlave())
				hijo.setLlave(true);

			// Recoger moneda
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
			int proximaCatapulta = mapa.mapaCatapultas[newY][newX];
			int idProximaCatapulta = mapa.idCatapultas[newY][newX];
			if (proximaCatapulta != -1 &&
				!isCatapultaActivada(idProximaCatapulta) &&
				getMoneda() == 0) {
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

	/**
	 * Devuelve el número de monedas que se tienen actualmente (0-5). Si devuelve 7, 
	 * el nodo está muerto (ha perdido más monedas de las que tenía o ha caído al agua sin estar volando).
	 * @return
	 */
	public int getMoneda() {
		// (00000111) para leer los 3 primeros bits
		return flags & 0x07; 
	}

	/**
	 * Establece el número de monedas (0-5) en el nodo actual.
	 * Si se establece un valor menor que 0, se considera que el nodo está muerto
	 * @param moneda
	 */
	protected void setMoneda(int moneda) {
		// Limpiamos los 3 primeros bits y escribimos el nuevo valor (0-5)
		if (moneda < 0) moneda = 7; // Valor 7 significa muerto (ha perdido mas monedas de las que tenía o ha caido al agua)
		flags = (byte) ((flags & ~0x07) | (moneda & 0x07));
	}

	/**
	 * Suma una moneda al nodo. Devuelve true si se ha podido recoger la moneda
	 * @return
	 */
	public boolean masMoneda() {
		int moneda = getMoneda();
		if (moneda < 5) {
			setMoneda(moneda + 1);
			return true;
		}
		return false; // No se puede recoger más monedas
	}

	/**
	 * Resta una moneda al nodo. Si el número de monedas llega a 0, el nodo se considera muerto.
	 */
	public void menosMoneda() {
		setMoneda(getMoneda() - 1);
	}

	/**
	 * Devuelve true si se tiene la llave, false si no se tiene
	 * @return
	 */
	public boolean hasLlave() {
		// (00001000 en binario) para leer el 4to bit
		return (flags & 0x08) != 0; 
	}

	/**
	 * Establece el estado de la llave (true si se tiene, false si no)
	 * @param llave
	 */
	public void setLlave(boolean llave) {
		if (llave) flags |= 0x08;	  // Enciende el bit
		else	   flags &= ~0x08;	 // Apaga el bit
	}

	/**
	 * Devuelve 0 si no está volando, o un valor entre 1 y 4 indicando la dirección de vuelo
	 * 1 = norte, 2 = sur, 3 = este, 4 = oeste 
	 */
	public byte isVolando() {
		return (byte) ((flags >> 4) & 0x07); // (01110000) para leer los bits 4-6 
	}

	/**
	 * Establece la dirección de vuelo (0 para no volando, 1 norte, 2 sur, 3 este, 4 oeste)
	 */
	public void setVolando(int direccion) {
		if (direccion < 0 || direccion > 4) {
			direccion = 0; // No volando
		}
		// 1. Limpiamos los bits 4, 5 y 6 usando la máscara 10001111 (0x8F)
		// 2. Desplazamos la nueva dirección 4 posiciones a la izquierda
		// 3. Unimos todo con un OR bit a bit (|)
		flags = (byte) ((flags & 0x8F) | ((direccion & 0x07) << 4));
	}

	/**
	 * Establece el estado de vuelo según el tipo de catapulta (1 norte, 2 sur, 3 este, 4 oeste)
	 * @param tipoCatapulta
	 */
	public void setVolandoCat(int tipoCatapulta) {
		if (tipoCatapulta == CATSOUTH) setVolando(DIRSOUTH);
		else if (tipoCatapulta == CATNORTH) setVolando(DIRNORTH);
		else if (tipoCatapulta == CATEAST) setVolando(DIREAST);
		else if (tipoCatapulta == CATWEST) setVolando(DIRWEST);
	}

	/**
	 * Devuelve true si la catapulta con el ID dado ha sido activada, false si no
	 * @param id
	 * @return
	 */
	private boolean isMonedaRecogida(int id) {
		if (id < 0 || id >= 128) return true; // ID fuera de rango
		int bloque = id / 64;
		int bit = id & 63; // id % 64
		if (bloque == 0)
			return (monedas1 & (1L << bit)) != 0;
		else
			return (monedas2 & (1L << bit)) != 0;
	}

	/**
	 * Marca la moneda con el ID dado como recogida en el nodo actual
	 * @param id
	 */
	private void marcarMonedaRecogida(int id) {
		if (id < 0 || id >= 128) return;
		int bloque = id / 64;
		int bit = id & 63; // id % 64
		if (bloque == 0)
			monedas1 |= (1L << bit);
		else
			monedas2 |= (1L << bit);
	}

	/**
	 * Devuelve true si la catapulta con el ID dado ha sido activada, false si no
	 * @param id
	 * @return
	 */
	 private boolean isCatapultaActivada(int id) {
		if (id < 0 || id >= 128) return true; // ID fuera de rango
		int bloque = id / 64;
		int bit = id & 63; // id % 64
		if (bloque == 0)
			return (catapultas1 & (1L << bit)) != 0;
		else
			return (catapultas2 & (1L << bit)) != 0;
	}

	/**
	 * Marca la catapulta con el ID dado como activada en el nodo actual
	 * @param id
	 */
	private void marcarCatapultaActivada(int id) {
		if (id < 0 || id >= 128) return;
		int bloque = id / 64;
		int bit = id & 63; // id % 64
		if (bloque == 0)
			catapultas1 |= (1L << bit);
		else
			catapultas2 |= (1L << bit);
	}

	/**
	 * Devuelve true si la posición (x,y) está fuera de los límites del mapa, false si está dentro
	 * @param x
	 * @param y
	 * @param mapa
	 * @return
	 */
	private boolean outOfBounds(int x, int y, Mapa mapa) {
		return x < 0 || x >= mapa.xmax || y < 0 || y >= mapa.ymax;
	}
	

	// --- OPERACIONES ---

	/**
	 * Devuelve true si el nodo se considera muerto (ha perdido todas las monedas o ha caído al agua sin estar volando), false si no
	 * @param mapa
	 * @return
	 */
	public boolean estaMuerto(Mapa mapa) {
		if (isVolando() == 0 && getCasilla(mapa) == AGUA)
			return true;
		return getMoneda() == 7;
	}

	/**
	 * Devuelve el tipo de casilla en la que se encuentra el nodo según el mapa dado (PARED, AGUA, PUERTA, VACIO, etc.)
	 * @param mapa
	 * @return
	 */
	public int getCasilla(Mapa mapa) {
		return mapa.grid[y][x];
	}

	/**
	 * Da la acción que se tomó para llegar a este nodo desde su padre. O null si es el nodo raíz.
	 * @return
	 */
	public ACTIONS getAccionPadre() {
		return accionPrecedente;
	}

	/**
	 * Devuelve el nodo padre del nodo actual.
	 * @return
	 */
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

	public boolean esMeta(Mapa mapa) {
		return this.x == mapa.portalX && this.y == mapa.portalY;
	}

	// FUNCIONES PARA ALGORITMOS INFORMADOS

	@Override
    public int compareTo(Nodo otro) {
        //  menor f(n)
        if (this.f != otro.f) {
            return Integer.compare(this.f, otro.f);
        }
        
        // menor h(n)
        if (this.h != otro.h) {
            return Integer.compare(this.h, otro.h);
        }
        
        // el nodo más antiguo (menor ID)
        return Integer.compare(this.idInsercion, otro.idInsercion);
    }

}