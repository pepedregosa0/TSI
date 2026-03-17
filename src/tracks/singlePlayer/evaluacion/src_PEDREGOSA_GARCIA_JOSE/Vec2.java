package tracks.singlePlayer.evaluacion.src_PEDREGOSA_GARCIA_JOSE;

// Estructura inmutable ultra ligera para coordenadas
public record Vec2(short x, short y) {
    // Método útil para saber a qué distancia está un objetivo
    public int distanciaManhattan(Vec2 otro) {
        return Math.abs(this.x - otro.x) + Math.abs(this.y - otro.y);
    }
}