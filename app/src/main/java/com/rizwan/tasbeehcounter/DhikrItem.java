package com.rizwan.tasbeehcounter;

public final class DhikrItem {
    public final String id;
    public final String title;
    public final String arabic;
    public final String translation;
    public final String explanation;
    public final int defaultTarget;
    public final boolean reportedTarget;
    public final boolean longText;
    public final String sourceNote;
    public final String[] sequencePhrases;
    public final String[] sequenceTranslations;
    public final int[] sequenceTargets;

    public DhikrItem(
            String id,
            String title,
            String arabic,
            String translation,
            int defaultTarget,
            boolean reportedTarget,
            boolean longText,
            String sourceNote) {
        this(id, title, arabic, translation, "", defaultTarget, reportedTarget, longText,
                sourceNote, null, null, null);
    }

    public DhikrItem(
            String id,
            String title,
            String arabic,
            String translation,
            String explanation,
            int defaultTarget,
            boolean reportedTarget,
            boolean longText,
            String sourceNote) {
        this(id, title, arabic, translation, explanation, defaultTarget, reportedTarget,
                longText, sourceNote, null, null, null);
    }

    public DhikrItem(
            String id,
            String title,
            String arabic,
            String translation,
            String explanation,
            int defaultTarget,
            boolean reportedTarget,
            boolean longText,
            String sourceNote,
            String[] sequencePhrases,
            String[] sequenceTranslations,
            int[] sequenceTargets) {
        this.id = id;
        this.title = title;
        this.arabic = arabic;
        this.translation = translation;
        this.explanation = explanation == null ? "" : explanation;
        this.defaultTarget = defaultTarget;
        this.reportedTarget = reportedTarget;
        this.longText = longText;
        this.sourceNote = sourceNote == null ? "" : sourceNote;
        this.sequencePhrases = sequencePhrases;
        this.sequenceTranslations = sequenceTranslations;
        this.sequenceTargets = sequenceTargets;
    }

    public boolean isSequence() {
        return sequencePhrases != null
                && sequenceTranslations != null
                && sequenceTargets != null
                && sequencePhrases.length > 0
                && sequencePhrases.length == sequenceTranslations.length
                && sequencePhrases.length == sequenceTargets.length;
    }
}
