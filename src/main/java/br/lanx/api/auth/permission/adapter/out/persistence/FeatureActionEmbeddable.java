package br.lanx.api.auth.permission.adapter.out.persistence;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Par (funcionalidade, ação extra): uma linha das tabelas *_actions. */
@Embeddable
public class FeatureActionEmbeddable {

    @Column(name = "feature", nullable = false, length = 100)
    private String feature;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    protected FeatureActionEmbeddable() {
    }

    public FeatureActionEmbeddable(String feature, String action) {
        this.feature = feature;
        this.action = action;
    }

    public String getFeature() {
        return feature;
    }

    public String getAction() {
        return action;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FeatureActionEmbeddable other
                && Objects.equals(feature, other.feature)
                && Objects.equals(action, other.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feature, action);
    }
}
