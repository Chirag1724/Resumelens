package com.resumelens.algorithm;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Self-implemented TF-IDF calculator.
 * No external NLP library - per ARCHITECTURE.md and PROJECT_CONTEXT.md.
 */
@Component
public class TfIdfCalculator {

    private static final Set<String> STOPWORDS = Set.of(
        "a","an","the","and","or","but","in","on","at","to","for","of","with",
        "by","from","is","are","was","were","be","been","being","have","has",
        "had","do","does","did","will","would","could","should","may","might",
        "it","its","this","that","these","those","i","me","my","we","our",
        "you","your","he","she","they","their","not","no","as","up","out"
    );

    /**
     * Generic job description and resume words that must never become keywords.
     * Contains over 150 words per SCORING_RULES.md.
     */
    public static final Set<String> GENERIC_WORDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
        // Explicitly mandated terms
        "responsibilities", "requirements", "requirement", "hiring", "apply", "large",
        "using", "related", "strong", "experience", "team", "work", "ability",
        "knowledge", "looking", "join", "role", "candidate", "years", "production",
        // Additional common verbs and verb forms
        "collaborate", "collaborating", "collaboration", "collaborative",
        "communicate", "communicating", "communication", "communications",
        "lead", "leading", "leads", "leader", "leadership",
        "manage", "managing", "manager", "management",
        "develop", "developing", "developed", "developer", "development",
        "create", "creating", "created", "creation",
        "build", "building", "built", "builder",
        "design", "designing", "designed", "designer",
        "deliver", "delivering", "delivered", "delivery",
        "maintain", "maintaining", "maintained", "maintenance",
        "support", "supporting", "supported",
        "ensure", "ensuring", "ensured",
        "help", "helping", "helped",
        "assist", "assisting", "assisted",
        "provide", "providing", "provided",
        "require", "requiring", "required",
        "prefer", "preferring", "preferred",
        "include", "including", "included", "includes",
        "perform", "performing", "performed",
        "participate", "participating", "participated",
        "contribute", "contributing", "contributed",
        "drive", "driving", "driven",
        "improve", "improving", "improved", "improvement",
        "increase", "increasing", "increased",
        "reduce", "reducing", "reduced",
        "achieve", "achieving", "achieved",
        "learn", "learning", "learned",
        "train", "training", "trained",
        "grow", "growing", "growth",
        "understand", "understanding",
        "implement", "implementing", "implemented", "implementation",
        "operate", "operating", "operation", "operations",
        "execute", "executing", "execution",
        "focus", "focusing", "focused",
        "solve", "solving", "solution", "solutions",
        "coordinate", "coordinating", "coordinated",
        "track", "tracking", "tracked",
        "deploy", "deploying", "deployment",
        "test", "testing", "tested",
        "write", "writing", "written",
        // Common adjectives and adverbs
        "excellent", "good", "great", "best", "high", "highly", "well",
        "proven", "solid", "deep", "broad", "wide", "fast", "paced",
        "dynamic", "passionate", "motivated", "enthusiastic", "dedicated",
        "successful", "successfully", "effective", "effectively",
        "innovative", "creative", "exceptional", "outstanding",
        "proficient", "proficiency", "skilled", "skills", "skill", "qualify", "qualified", "qualification", "qualifications",
        "flexible", "detail", "oriented", "hands", "ideal", "critical", "key",
        "senior", "junior", "mid", "entry", "principal", "staff",
        "full", "time", "part", "contract", "remote", "hybrid", "onsite",
        // Common nouns and filler
        "position", "positions", "job", "jobs", "career", "careers",
        "company", "business", "client", "clients", "customer", "customers",
        "project", "projects", "product", "products", "program", "programs",
        "opportunity", "opportunities", "environment", "industry", "organization",
        "degree", "bachelor", "bachelors", "master", "masters", "phd",
        "education", "background", "field", "area", "discipline",
        "professional", "professionals", "individual", "individuals", "person", "people",
        "member", "members", "partner", "partners", "stakeholder", "stakeholders",
        "daily", "weekly", "monthly", "quarterly", "annual",
        "year", "month", "months", "day", "days",
        "level", "range", "scope", "scale", "aspect", "aspects",
        "need", "needs", "must", "plus", "nice", "have", "within", "across",
        "models", "model", "data",
        // Keyword noise additions
        "engineer", "engineers", "practices", "practice", "evaluate",
        "techniques", "technique", "science", "apache", "computer science"
    )));

    public static final Set<String> JD_FILLER = GENERIC_WORDS;

    /**
     * Common English words set per SCORING_RULES.md (at least 300 words).
     * Must never be added during the TF-IDF keyword fill step.
     */
    public static final Set<String> COMMON_ENGLISH_WORDS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
        // Explicitly mandated words
        "use", "user", "users", "store", "clean", "style", "code", "web",
        "backend", "frontend", "services", "service", "interfaces", "applications", "application",
        "build", "develop", "design", "write", "work", "through", "stack", "sprint",
        "team", "project", "projects", "system", "systems", "tools", "tool", "data",

        // Additional common verbs, nouns, adjectives, and adverbs
        "ability", "able", "about", "above", "across", "action", "actions", "activity",
        "activities", "added", "adding", "addition", "additional", "address", "advance", "advanced",
        "advice", "after", "again", "against", "agile", "agree", "ahead", "allow",
        "allowed", "allowing", "almost", "along", "already", "also", "although", "always",
        "among", "amount", "another", "answer", "anyone", "anything", "anywhere", "appear",
        "applied", "apply", "applying", "approach", "approaches", "area", "areas", "around",
        "aspect", "aspects", "assist", "assistance", "assisting", "assume", "attempt", "attend",
        "audit", "author", "auto", "automate", "automated", "automatic", "available", "average",
        "avoid", "aware", "away", "back", "base", "based", "basic", "basically",
        "basis", "become", "before", "begin", "beginning", "behind", "believe", "below",
        "benefit", "benefits", "beside", "best", "better", "between", "beyond", "board",
        "body", "book", "both", "brief", "briefly", "bring", "broad", "broadly",
        "business", "businesses", "call", "called", "calling", "came", "care", "career",
        "careers", "case", "cases", "catch", "cause", "certain", "certainly", "chain",
        "chance", "change", "changed", "changes", "changing", "check", "checked", "checking",
        "choice", "choices", "choose", "chose", "chosen", "clear", "clearly", "close",
        "closely", "closer", "common", "commonly", "company", "companies", "compare", "complete",
        "completed", "completely", "complex", "complexity", "concept", "concepts", "condition", "conditions",
        "consider", "considered", "consist", "constant", "consult", "continue", "continued", "control",
        "controls", "core", "corner", "could", "count", "course", "courses", "cover",
        "coverage", "covered", "create", "created", "creating", "creation", "cross", "current",
        "currently", "custom", "customer", "customers", "daily", "date", "dates", "deal",
        "decide", "decision", "decisions", "declare", "deep", "deeply", "define", "defined",
        "defining", "deliver", "delivered", "delivering", "delivery", "demand", "demands", "department",
        "depend", "depends", "describe", "described", "describing", "description", "descriptions", "detail",
        "detailed", "details", "determine", "device", "devices", "direct", "direction", "directly",
        "discover", "discuss", "divide", "document", "documents", "documentation", "done", "down",
        "drive", "driven", "driver", "driving", "drop", "during", "early", "easily",
        "easy", "edge", "effect", "effective", "effectively", "effects", "effort", "efforts",
        "either", "element", "elements", "enable", "enabled", "enables", "enabling", "ended",
        "ending", "energy", "engine", "engines", "engineer", "engineers", "engineering", "enough",
        "ensure", "ensured", "ensures", "ensuring", "enter", "enterprise", "entry", "environment",
        "environments", "equal", "error", "errors", "especially", "essential", "establish", "established",
        "event", "events", "every", "everyone", "everything", "everywhere", "exact", "exactly",
        "example", "examples", "except", "execute", "executed", "executing", "execution", "expect",
        "expected", "experience", "experienced", "experiences", "expert", "experts", "expertise", "explain",
        "extra", "fact", "factor", "factors", "failure", "failures", "fast", "feature",
        "features", "feel", "field", "fields", "figure", "figures", "final", "finally",
        "finding", "findings", "fine", "finish", "finished", "first", "flow", "flows",
        "focus", "focused", "focuses", "focusing", "follow", "followed", "following", "force",
        "form", "forms", "format", "formats", "forward", "found", "framework", "frameworks",
        "free", "front", "full", "fully", "function", "functional", "functions", "future",
        "gain", "gained", "general", "generally", "generate", "generated", "generating", "generation",
        "give", "given", "giving", "global", "goal", "goals", "good", "great",
        "greatly", "group", "groups", "grow", "growing", "growth", "guide", "guided",
        "guidelines", "guides", "half", "hand", "handle", "handled", "handles", "handling",
        "happen", "hard", "have", "having", "head", "header", "help", "helped",
        "helpful", "helping", "helps", "here", "high", "higher", "highest", "highly",
        "history", "hold", "home", "hope", "hour", "hours", "huge", "human",
        "idea", "ideas", "ideal", "identify", "identified", "impact", "impacts", "implement",
        "implementation", "implemented", "implementing", "important", "improve", "improved", "improvement", "improvements",
        "improving", "include", "included", "includes", "including", "increase", "increased", "increases",
        "increasing", "indeed", "individual", "individuals", "industry", "industries", "inform", "information",
        "initial", "initially", "inner", "input", "inputs", "inside", "insight", "insights",
        "instead", "integrate", "integrated", "integrating", "integration", "integrations", "intend", "interest",
        "internal", "introduce", "introduced", "introduction", "issue", "issues", "item", "items",
        "join", "joined", "joining", "keep", "keeping", "kept", "key", "keys",
        "kind", "kinds", "know", "knowing", "knowledge", "known", "label", "labels",
        "lack", "large", "larger", "largest", "last", "late", "later", "layer",
        "layers", "lead", "leader", "leaders", "leadership", "leading", "leads", "learn",
        "learned", "learning", "least", "leave", "less", "level", "levels", "life",
        "light", "like", "likely", "limit", "limited", "limits", "line", "lines",
        "link", "linked", "links", "list", "listed", "lists", "little", "live",
        "load", "loads", "local", "locate", "located", "location", "locations", "logic",
        "logical", "long", "longer", "look", "looked", "looking", "looks", "loop",
        "loops", "maintain", "maintained", "maintaining", "maintains", "maintenance", "major", "make",
        "makes", "making", "manage", "managed", "management", "manager", "managers", "manages",
        "managing", "manner", "many", "mark", "market", "match", "matched", "matches",
        "matching", "matter", "mean", "meaning", "means", "measure", "measured", "measurement",
        "meet", "meeting", "meetings", "member", "members", "mention", "mentioned", "message",
        "messages", "method", "methods", "methodology", "middle", "might", "million", "mind",
        "minute", "minutes", "miss", "mode", "model", "modeling", "models", "modern",
        "module", "modules", "moment", "month", "months", "more", "most", "mostly",
        "move", "moved", "movement", "moving", "much", "multi", "multiple", "must",
        "name", "names", "narrow", "natural", "near", "nearly", "necessary", "need",
        "needed", "needs", "network", "networks", "never", "next", "nice", "normal",
        "normally", "note", "noted", "notes", "notice", "novel", "number", "numbers",
        "object", "objects", "objective", "objectives", "obtain", "occur", "occurred", "offer",
        "offered", "office", "often", "once", "ongoing", "open", "operate", "operating",
        "operation", "operations", "operator", "operators", "opinion", "opportunity", "opportunities", "opt",
        "optimize", "optimized", "optimizing", "option", "options", "order", "ordered", "orders",
        "organization", "organizations", "organize", "organized", "original", "other", "others", "otherwise",
        "outcome", "outcomes", "output", "outputs", "overall", "overview", "pace", "package",
        "packages", "page", "pages", "pair", "pairs", "panel", "paper", "part",
        "partial", "partially", "particular", "particularly", "parties", "partner", "partners", "pass",
        "passed", "passing", "past", "path", "paths", "pattern", "patterns", "people",
        "per", "perform", "performance", "performed", "performing", "performs", "period", "periods",
        "person", "personal", "phase", "phases", "phrase", "pick", "picked", "piece",
        "pieces", "pipeline", "pipelines", "place", "places", "plan", "planned", "planning",
        "plans", "platform", "platforms", "point", "points", "policy", "policies", "poor",
        "popular", "portion", "portions", "position", "positions", "possible", "potential", "power",
        "powerful", "practice", "practices", "prefer", "preferred", "prepare", "prepared", "preparing",
        "present", "presentation", "presented", "preserve", "prevent", "previous", "previously", "primary",
        "principle", "principles", "priority", "priorities", "private", "problem", "problems", "procedure",
        "procedures", "process", "processed", "processes", "processing", "produce", "produced", "producing",
        "product", "production", "products", "professional", "professionals", "program", "programs", "progress",
        "project", "projects", "promise", "proper", "properly", "property", "properties", "propose",
        "proposed", "protect", "protocol", "protocols", "provide", "provided", "provider", "providers",
        "provides", "providing", "public", "pull", "purpose", "purposes", "push", "quality",
        "quarter", "quarterly", "question", "questions", "quick", "quickly", "quite", "raise",
        "raised", "range", "ranges", "rank", "rapid", "rapidly", "rate", "rates",
        "rather", "reach", "reached", "reaching", "reader", "reading", "ready", "real",
        "reality", "realize", "realized", "reason", "reasons", "receive", "received", "recent",
        "recently", "recognize", "record", "records", "reduce", "reduced", "reducing", "refer",
        "reference", "references", "regular", "regularly", "relate", "related", "relation", "relations",
        "relationship", "relationships", "release", "releases", "remain", "remains", "remote", "remove",
        "removed", "repeat", "replace", "report", "reported", "reporting", "reports", "represent",
        "represented", "request", "requests", "require", "required", "requirement", "requirements", "requires",
        "research", "resolve", "resolved", "resolving", "resource", "resources", "respect", "respond",
        "response", "responses", "responsible", "responsibilities", "result", "resulted", "resulting", "results",
        "return", "returned", "reveal", "review", "reviewed", "reviewing", "reviews", "right",
        "rise", "risk", "risks", "role", "roles", "room", "root", "round",
        "routine", "rule", "rules", "running", "safe", "safety", "sample", "samples",
        "scale", "scenario", "scenarios", "scheme", "scope", "score", "scores", "scoring",
        "screen", "screens", "seamless", "search", "searching", "second", "secondary", "section",
        "sections", "sector", "secure", "security", "seek", "seeking", "select", "selected",
        "selection", "senior", "sense", "separate", "sequence", "series", "serve", "served",
        "server", "servers", "serves", "serving", "session", "sessions", "setup", "several",
        "shape", "share", "shared", "sharing", "sheet", "shift", "short", "should",
        "show", "showed", "showing", "shown", "shows", "side", "sides", "sign",
        "signal", "signals", "significant", "significantly", "similar", "similarly", "simple", "simplified",
        "simply", "single", "site", "sites", "situation", "situations", "size", "sizes",
        "skilled", "skills", "slow", "small", "smaller", "smart", "smooth", "solid",
        "solution", "solutions", "solve", "solved", "solves", "solving", "sound", "source",
        "sources", "space", "spaces", "special", "specific", "specifically", "specifications", "speed",
        "stage", "stages", "standard", "standards", "start", "started", "starting", "starts",
        "state", "statement", "statements", "states", "static", "step", "steps", "still",
        "stop", "straight", "strategy", "strategies", "stream", "streaming", "streams", "strength",
        "strengths", "strict", "strictly", "strong", "strongly", "structure", "structured", "structures",
        "struggle", "study", "subject", "submit", "submitted", "succeed", "success", "successful",
        "successfully", "suggest", "suggestion", "suggestions", "suit", "suitable", "suite", "summary",
        "support", "supported", "supporting", "supports", "sure", "surface", "switch", "table",
        "tables", "target", "targets", "task", "tasks", "tech", "technical", "technique",
        "techniques", "technology", "technologies", "term", "terms", "test", "tested", "testing",
        "tests", "than", "their", "theory", "therefore", "thing", "things", "think",
        "third", "though", "thought", "thousands", "threat", "three", "tier", "tiers",
        "time", "times", "timing", "title", "today", "together", "topic", "topics",
        "total", "track", "tracked", "tracking", "tracks", "trade", "traffic", "train",
        "trained", "training", "transfer", "transition", "treat", "trend", "trends", "trigger",
        "trouble", "true", "trust", "turn", "turned", "turning", "turns", "twice",
        "type", "types", "typical", "typically", "understand", "understanding", "unit", "units",
        "update", "updated", "updates", "updating", "upon", "usage", "useful", "valid",
        "validate", "validated", "validation", "value", "values", "variable", "variables", "variety",
        "various", "vary", "vast", "verify", "version", "versions", "view", "viewed",
        "viewing", "views", "volume", "wait", "want", "wanted", "wants", "wave",
        "ways", "weak", "week", "weekly", "weeks", "weight", "weights", "well",
        "whole", "wide", "widely", "width", "wish", "within", "without", "word",
        "words", "work", "worked", "worker", "workers", "working", "works", "world",
        "worth", "wrap", "writing", "written", "year", "yearly", "years", "yield", "zero"
    )));

    public static final Set<String> COMMON_WORDS = COMMON_ENGLISH_WORDS;

    /** TF: raw count of term in document normalised by document length. */
    public Map<String, Double> termFrequency(String text) {
        String[] tokens = tokenize(text);
        Map<String, Double> tf = new HashMap<>();
        for (String token : tokens) {
            if (!STOPWORDS.contains(token)) {
                tf.merge(token, 1.0, (a, b) -> a + b);
            }
        }
        // Normalise by document length
        int total = tokens.length;
        if (total > 0) tf.replaceAll((k, v) -> v / total);
        return tf;
    }

    /**
     * IDF across a list of documents.
     * IDF(t) = log((1 + N) / (1 + df(t))) + 1  (smoothed, sklearn-style)
     */
    public Map<String, Double> idf(List<String> documents) {
        int n = documents.size();
        Map<String, Integer> df = new HashMap<>();
        for (String doc : documents) {
            Set<String> terms = Arrays.stream(tokenize(doc))
                    .filter(t -> !STOPWORDS.contains(t))
                    .collect(Collectors.toSet());
            terms.forEach(t -> df.merge(t, 1, (a, b) -> a + b));
        }
        Map<String, Double> idfMap = new HashMap<>();
        df.forEach((term, count) ->
            idfMap.put(term, Math.log((1.0 + n) / (1.0 + count)) + 1.0)
        );
        return idfMap;
    }

    /**
     * TF-IDF vector for a document given a pre-computed IDF map.
     * Reuse idf() across the resume+JD pair for consistency.
     */
    public Map<String, Double> tfidfVector(String text, Map<String, Double> idfMap) {
        Map<String, Double> tf = termFrequency(text);
        Map<String, Double> result = new HashMap<>();
        tf.forEach((term, tfVal) -> {
            double idfVal = idfMap.getOrDefault(term, 0.0);
            result.put(term, tfVal * idfVal);
        });
        return result;
    }

    /**
     * Returns top N terms ranked for job description keyword extraction (max 30).
     * 1. First take every JD term that is in the skill dictionary, including multiword
     *    dictionary terms matched as a phrase, in order of occurrence.
     * 2. Fill remaining slots with the highest TF-IDF terms using the sentence-level
     *    corpus already in place, excluding the generic set and tokens shorter than 3 characters.
     * 3. Keeps multiword skills as one keyword (e.g. deep learning is not split into deep and learning).
     */
    public List<String> topKeywords(String text, int topN) {
        if (text == null || text.isBlank() || topN <= 0) return List.of();

        // 1. Extract terms in the skill dictionary, including multiword phrases
        List<String> extractedSkills = extractSkillsFromText(text);

        if (extractedSkills.size() >= topN) {
            return extractedSkills.subList(0, topN);
        }

        // 2. Sentence-level IDF corpus for remaining slots
        List<String> lines = new ArrayList<>();
        for (String segment : text.split("[\\n.!?;•\u2013-]+")) {
            String trimmed = segment.trim();
            if (!trimmed.isBlank()) {
                lines.add(trimmed);
            }
        }
        if (lines.isEmpty()) lines = List.of(text);

        Map<String, Double> idfMap = idf(lines);
        Map<String, Double> tfidfVec = tfidfVector(text, idfMap);

        // Keep multiword skill as one keyword: do not split or add component words
        Set<String> wordsInMultiwordSkills = new HashSet<>();
        for (String skill : extractedSkills) {
            if (skill.contains(" ") || skill.contains("-")) {
                for (String part : skill.split("[\\s\\-]+")) {
                    if (!part.isBlank()) {
                        wordsInMultiwordSkills.add(part.toLowerCase(Locale.ROOT));
                    }
                }
            }
        }

        Set<String> alreadyExtracted = new HashSet<>(extractedSkills);
        List<String> result = new ArrayList<>(extractedSkills);

        List<Map.Entry<String, Double>> entries = new ArrayList<>(tfidfVec.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int maxFill = (extractedSkills.size() < 3) ? 10 : 8;
        boolean requireTechnicalShape = (extractedSkills.size() >= 3);
        int fillCount = 0;

        for (Map.Entry<String, Double> entry : entries) {
            String term = entry.getKey();
            if (term.length() < 3) continue;
            if (GENERIC_WORDS.contains(term)) continue;
            if (COMMON_ENGLISH_WORDS.contains(term)) continue;
            if (alreadyExtracted.contains(term)) continue;
            if (wordsInMultiwordSkills.contains(term)) continue;
            if (entry.getValue() <= 0.0) continue;

            if (requireTechnicalShape && !looksTechnicalInText(term, text)) {
                continue;
            }

            if (result.size() >= topN) break;
            result.add(term);
            fillCount++;
            if (fillCount >= maxFill) break;
        }

        return Collections.unmodifiableList(result);
    }

    /**
     * Checks if a term looks technical in the original job description text.
     * Criteria per SCORING_RULES.md:
     * - contains a digit, dot, plus, hash or slash, OR
     * - is written in camel case, OR
     * - is written in all capitals, OR
     * - is capitalized in the middle of a sentence.
     */
    public static boolean looksTechnicalInText(String term, String text) {
        if (term == null || term.isBlank() || text == null || text.isBlank()) {
            return false;
        }
        String lowerTerm = term.toLowerCase(Locale.ROOT);
        String termNorm = lowerTerm.replaceAll("[^a-z0-9]", "");
        if (termNorm.isEmpty()) return false;

        String[] sentences = text.split("[\\n\\r.!?;•\u2013\u2014]+");
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (trimmed.isBlank()) continue;

            String[] words = trimmed.split("\\s+");
            int firstAlphaWordIdx = -1;
            for (int i = 0; i < words.length; i++) {
                if (hasLetter(words[i])) {
                    firstAlphaWordIdx = i;
                    break;
                }
            }

            for (int i = 0; i < words.length; i++) {
                String rawWord = words[i];
                String stripped = stripBoundaryPunctuation(rawWord);
                if (stripped.isBlank()) continue;

                if (!matchesTerm(rawWord, stripped, termNorm)) {
                    continue;
                }

                if (containsTechnicalChar(rawWord) || containsTechnicalChar(stripped)) {
                    return true;
                }
                if (isCamelCase(stripped)) {
                    return true;
                }
                if (isAllCaps(stripped)) {
                    return true;
                }
                if (firstAlphaWordIdx >= 0 && i > firstAlphaWordIdx && Character.isUpperCase(stripped.charAt(0))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasLetter(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLetter(s.charAt(i))) return true;
        }
        return false;
    }

    private static boolean matchesTerm(String rawWord, String strippedWord, String termNorm) {
        String strippedNorm = strippedWord.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        if (strippedNorm.equals(termNorm)) {
            return true;
        }
        String[] parts = rawWord.toLowerCase(Locale.ROOT).split("[^a-z0-9]+");
        for (String p : parts) {
            if (p.equals(termNorm)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsTechnicalChar(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c) || c == '.' || c == '+' || c == '#' || c == '/') {
                return true;
            }
        }
        return false;
    }

    private static boolean isCamelCase(String s) {
        boolean seenLower = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLowerCase(c)) {
                seenLower = true;
            } else if (seenLower && Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAllCaps(String s) {
        int letters = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c)) {
                if (!Character.isUpperCase(c)) {
                    return false;
                }
                letters++;
            }
        }
        return letters >= 2;
    }

    private static String stripBoundaryPunctuation(String s) {
        if (s == null) return "";
        int start = 0;
        int end = s.length();
        while (start < end && isBoundaryPunctuation(s.charAt(start))) {
            start++;
        }
        while (end > start && isBoundaryPunctuation(s.charAt(end - 1))) {
            end--;
        }
        return s.substring(start, end);
    }

    private static boolean isBoundaryPunctuation(char c) {
        return c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}'
                || c == '"' || c == '\'' || c == '`' || c == ',' || c == ';' || c == ':'
                || c == '!' || c == '?' || c == '<' || c == '>';
    }

    private List<String> extractSkillsFromText(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        List<SpanMatch> spans = new ArrayList<>();

        for (String skill : SkillDictionary.getSkillsSortedByLengthDesc()) {
            if (GENERIC_WORDS.contains(skill)) continue;
            int fromIdx = 0;
            while (fromIdx < lower.length()) {
                int idx = lower.indexOf(skill, fromIdx);
                if (idx == -1) break;
                int endIdx = idx + skill.length();

                boolean validBefore = (idx == 0) || !Character.isLetterOrDigit(lower.charAt(idx - 1));
                if (validBefore && skill.equals("net") && idx > 0 && lower.charAt(idx - 1) == '.') {
                    validBefore = false;
                }
                if (validBefore && skill.equals("c") && idx > 0 && (lower.charAt(idx - 1) == '+' || lower.charAt(idx - 1) == '#')) {
                    validBefore = false;
                }

                boolean validAfter = (endIdx == lower.length()) || !Character.isLetterOrDigit(lower.charAt(endIdx));
                if (validAfter && skill.equals("c") && endIdx < lower.length() && (lower.charAt(endIdx) == '+' || lower.charAt(endIdx) == '#')) {
                    validAfter = false;
                }
                if (validAfter && skill.endsWith("+") && endIdx < lower.length() && lower.charAt(endIdx) == '+') {
                    validAfter = false;
                }

                if (validBefore && validAfter) {
                    boolean overlaps = false;
                    for (SpanMatch sm : spans) {
                        if (idx < sm.end && endIdx > sm.start) {
                            overlaps = true;
                            break;
                        }
                    }
                    if (!overlaps) {
                        spans.add(new SpanMatch(idx, endIdx, skill));
                    }
                }
                fromIdx = idx + 1;
            }
        }

        spans.sort(Comparator.comparingInt(sm -> sm.start));
        Set<String> seen = new LinkedHashSet<>();
        for (SpanMatch sm : spans) {
            seen.add(sm.skill);
        }
        return new ArrayList<>(seen);
    }

    private record SpanMatch(int start, int end, String skill) {}

    public static String[] tokenize(String text) {
        return text.toLowerCase(Locale.ROOT)
                   .replaceAll("[^a-z0-9\\s]", " ")
                   .trim()
                   .split("\\s+");
    }
}