package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

import java.util.ArrayList;
import java.util.HashSet;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tracks.singlePlayer.MetricsProvider;
import tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Nodo;
import tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE.Mapa;

// Nota: Se puede heredar de otras clases personalizadas por el alumnado que hereden de AbstractPlayer para generalizar
// los elementos comunes a todos los algoritmos.

public class AgenteProfundidad extends AbstractPlayer {
	private Nodo nodoActual;
	private Mapa mapa;

	private int nodosExpandidos = 0;
	private int profundidadMaxima = 0;
	private long tiempoEjecucion;

	private ArrayList<ACTIONS> plan = null;

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

	private void imprimirMetricas() {
		MetricsProvider.getInstance().setNumAccionesPlan(plan.size());
		MetricsProvider.getInstance().setNodosExpandidos(nodosExpandidos);
		MetricsProvider.getInstance().setProfundidadMaxima(profundidadMaxima);
		MetricsProvider.getInstance().setTiempoMilisegundos(tiempoEjecucion / 1000000);
		MetricsProvider.getInstance().printMetrics();
	}
}
