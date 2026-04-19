package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import static tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Constantes.*;
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
	 * Constructor para el nodo raíz o estado inicial de la búsqueda.
	 * @param x Coordenada X inicial del avatar en el mapa.
	 * @param y Coordenada Y inicial del avatar en el mapa.
	 * @param moneda Cantidad inicial de monedas en el inventario (0-5).
	 * @param llave Indica si el avatar posee la llave (true) o no (false).
	 * @param volando Dirección de vuelo actual (0: no volando, 1: norte, 2: sur, 3: este, 4: oeste).
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
	 * @param x Coordenada X de la nueva posición.
	 * @param y Coordenada Y de la nueva posición.
	 * @param padre Nodo padre del cual se copia el estado.
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
	 * @param mapa Mapa que se utiliza para determinar las casillas, monedas, catapultas, etc. en el entorno
	 * @return Una lista de nodos hijos que representan los estados alcanzables desde el nodo actual aplicando las acciones posibles.
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
	 * @return Número de monedas (0-5) o 7 si el nodo está muerto.
	 */
	public int getMoneda() {
		// (00000111) para leer los 3 primeros bits
		return flags & 0x07; 
	}

	/**
	 * Establece el número de monedas (0-5) en el nodo actual.
	 * Si se establece un valor menor que 0, se considera que el nodo está muerto
	 * @param moneda Número de monedas a establecer (0-5). Si es menor que 0, el nodo se considera muerto.
	 */
	protected void setMoneda(int moneda) {
		// Limpiamos los 3 primeros bits y escribimos el nuevo valor (0-5)
		if (moneda < 0) moneda = 7; // Valor 7 significa muerto (ha perdido mas monedas de las que tenía o ha caido al agua)
		flags = (byte) ((flags & ~0x07) | (moneda & 0x07));
	}

	/**
	 * Suma una moneda al nodo. Devuelve true si se ha podido recoger la moneda
	 * @return true si se ha podido recoger la moneda, false en caso contrario
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
	 * @return true si se tiene la llave, false si no se tiene
	 */
	public boolean hasLlave() {
		// (00001000 en binario) para leer el 4to bit
		return (flags & 0x08) != 0; 
	}

	/**
	 * @param llave true para indicar que se tiene la llave, false para indicar que no se tiene
	 */
	public void setLlave(boolean llave) {
		if (llave) flags |= 0x08;	  // Enciende el bit
		else	   flags &= ~0x08;	 // Apaga el bit
	}

	/**
	 * @return 0 si no está volando, o un valor entre 1 y 4 indicando la dirección de vuelo
	 * 1 = norte, 2 = sur, 3 = este, 4 = oeste 
	 */
	public byte isVolando() {
		return (byte) ((flags >> 4) & 0x07); // (01110000) para leer los bits 4-6 
	}

	/**
	 * @param direccion Establece la dirección de vuelo (0 para no volando, 1 norte, 2 sur, 3 este, 4 oeste)
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
	 * @param tipoCatapulta El tipo de catapulta
	 */
	public void setVolandoCat(int tipoCatapulta) {
		if (tipoCatapulta == CATSOUTH) setVolando(DIRSOUTH);
		else if (tipoCatapulta == CATNORTH) setVolando(DIRNORTH);
		else if (tipoCatapulta == CATEAST) setVolando(DIREAST);
		else if (tipoCatapulta == CATWEST) setVolando(DIRWEST);
	}

	/**
	 * Devuelve true si la catapulta con el ID dado ha sido activada, false si no
	 * @param id El ID de la catapulta
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
	 * @param id El ID de la moneda
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
	 * @param id El ID de la catapulta
	 * @return true si la catapulta con el ID dado ha sido activada, false si no
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
	 * @param id El ID de la catapulta
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
	 * @param x Coordenada X a verificar
	 * @param y Coordenada Y a verificar
	 * @param mapa Mapa que se utiliza para determinar los límites (xmax, ymax)
	 * @return true si la posición (x,y) está fuera de los límites del mapa, false si está dentro
	 */
	private boolean outOfBounds(int x, int y, Mapa mapa) {
		return x < 0 || x >= mapa.xmax || y < 0 || y >= mapa.ymax;
	}
	

	// --- OPERACIONES ---

	/**
	 * @param mapa Mapa que se utiliza para determinar el tipo de casilla actual y si el nodo ha caído al agua sin estar volando
	 * @return true si el nodo está muerto (ha perdido más monedas de las que tenía o ha caído al agua sin estar volando), false en caso contrario
	 */
	public boolean estaMuerto(Mapa mapa) {
		if (isVolando() == 0 && getCasilla(mapa) == AGUA)
			return true;
		return getMoneda() == 7;
	}

	/**
	 * @param mapa Mapa que se utiliza para determinar el tipo de casilla en la posición actual del nodo
	 * @return El tipo de casilla en la posición actual del nodo según el mapa (PARED, AGUA, PUERTA, etc.)
	 */
	public int getCasilla(Mapa mapa) {
		return mapa.grid[y][x];
	}

	/**
	 * @return La acción que se tomó para llegar a este nodo desde su padre null para el nodo raíz
	 */
	public ACTIONS getAccionPadre() {
		return accionPrecedente;
	}

	/**
	 * @return El nodo padre desde el cual se llegó a este nodo null para el nodo raíz
	 */
	public Nodo getPadre() {
		return padre;
	}

	// DEPURACION
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

	// PARA USAR HASHMAP O HASHSET DE NODOS
	@Override
	public int hashCode() {
		return Objects.hash(x, y, flags, monedas1, monedas2, catapultas1, catapultas2);
	}

	/**
	 * @param mapa Mapa que se utiliza para determinar la posición del portal/meta
	 * @return true si el nodo actual está en la posición del portal/meta según el mapa, false en caso contrario
	 */
	public boolean esMeta(Mapa mapa) {
		return this.x == mapa.portalX && this.y == mapa.portalY;
	}

	// FUNCIONES PARA ALGORITMOS INFORMADOS
	// El orden de comparación es: menor f(n), luego menor h(n) y finalmente el nodo más antiguo (menor ID)
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