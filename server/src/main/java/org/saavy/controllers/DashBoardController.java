package org.saavy.controllers;

import org.saavy.component.DashboardCardProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class DashBoardController extends BaseDashBoardController {

    @Autowired(required = false)
    private List<DashboardCardProvider> cardProviders;

    @Override
    public List<Map<String, Object>> getCards() {
        List<Map<String, Object>> allCards = new ArrayList<>();
        if (cardProviders != null) {
            for (DashboardCardProvider provider : cardProviders) {
                try {
                    List<Map<String, Object>> cards = provider.getCards();
                    if (cards != null) {
                        allCards.addAll(cards);
                    }
                } catch (Exception e) {
                    // Prevent single provider error from failing the whole dashboard endpoint
                }
            }
        }
        return allCards;
    }
}
