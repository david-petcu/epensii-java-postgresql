package model;

import java.util.Map;

public record StatisticiDashboard(Map<StatusCerere, Integer> numarPeStatus,
                                  double totalPensiiInPlata) {
    public int numar(StatusCerere status) {
        return numarPeStatus.getOrDefault(status, 0);
    }
}
