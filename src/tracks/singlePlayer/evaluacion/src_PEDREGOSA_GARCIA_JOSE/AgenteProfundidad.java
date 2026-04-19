package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import java.util.ArrayList;
import java.util.HashSet;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.MetricsProvider;

public class AgenteProfundidad extends AbstractPlayer {
	private Nodo nodoActual;
	private Mapa mapa;
	private ArrayList<ACTIONS> plan = null;

	// METRICAS
	private int nodosExpandidos = 0;
	private int profundidadMaxima = 0;
	private long tiempoEjecucion;


	public AgenteProfundidad(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		super();

		mapa = new Mapa(stateObs);
		nodoActual = new Nodo((short) mapa.posicionX, (short) mapa.posicionY, 0, false, (byte) 0);
	}

	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		if (plan == null) {
			HashSet<Nodo> visitados = new HashSet<>();
			long inicio = System.nanoTime();
			Nodo meta = DFSRecursivo(nodoActual, visitados);
			long fin = System.nanoTime();
			tiempoEjecucion = (fin - inicio);
			if (meta != null) {
				plan = reconstruirPlan(meta);
				imprimirMetricas();
			}
			else {
				plan = new ArrayList<>();
				plan.add(ACTIONS.ACTION_NIL);
			}
		}
		if (plan.size() > 0) {
			ACTIONS accion = plan.remove(0);
			//System.out.println("Accion: " + accion);
			return accion;
		}
		
		return ACTIONS.ACTION_NIL;
	}

	/**
	 * Búsqueda en profundidad recursiva
	 * @param actual Nodo actual a expandir
	 * @param visitados Conjunto de nodos ya visitados para evitar ciclos
	 * @return Nodo meta si se encuentra, null si no se encuentra en esta rama
	 */
	private Nodo DFSRecursivo(Nodo actual, HashSet<Nodo> visitados)
	{
		// Criterio de parada
		if (actual.esMeta(mapa))
			return actual;

		visitados.add(actual);
		// Expandimos el nodo actual
		ArrayList<Nodo> hijos = actual.expandir(mapa);
		nodosExpandidos++;
		for (Nodo hijo : hijos) {
			hijo.g = actual.g + 1;
			if (!visitados.contains(hijo)) {
				if (hijo.g > profundidadMaxima)
					profundidadMaxima = hijo.g;
				Nodo resultado = DFSRecursivo(hijo, visitados);
				if (resultado != null)
					return resultado;
			}
		}
		return null;
	}

	/**
	 * Reconstruye el plan desde el nodo meta hasta la raíz utilizando los nodos padre y las acciones que llevaron a cada nodo
	 * para generar la secuencia de acciones que forman el plan.
	 * @param meta Nodo meta encontrado por la búsqueda
	 * @return Lista de acciones que forman el plan desde el estado inicial hasta el estado meta.
	 */
	private ArrayList<ACTIONS> reconstruirPlan(Nodo meta) {
		ArrayList<ACTIONS> plan = new ArrayList<>();
		Nodo actual = meta;
		while (actual.getPadre() != null) {
			plan.add(0, actual.getAccionPadre());
			actual = actual.getPadre();
		}
		//System.out.println("Plan encontrado: " + plan);
		return plan;
	}

	/**
	 * Imprime las métricas de la búsqueda utilizando el MetricsProvider.
	 * Metricas incluidas:
	 * - Número de acciones en el plan encontrado
	 * - Número de nodos expandidos durante la búsqueda
	 * - Profundidad máxima alcanzada durante la búsqueda
	 * - Tiempo de ejecución en milisegundos
	 */
	private void imprimirMetricas() {
		MetricsProvider.getInstance().setNumAccionesPlan(plan.size());
		MetricsProvider.getInstance().setNodosExpandidos(nodosExpandidos);
		MetricsProvider.getInstance().setProfundidadMaxima(profundidadMaxima);
		MetricsProvider.getInstance().setTiempoMilisegundos(tiempoEjecucion / 1000000);
		MetricsProvider.getInstance().printMetrics();
	}
}
