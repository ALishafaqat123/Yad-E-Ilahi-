package com.rizwan.tasbeehcounter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class DhikrCatalog {
    public static final String CATEGORY_ISTIGHFAR = "category_istighfar";
    public static final String CATEGORY_TASBEEH = "category_tasbeeh";
    public static final String CATEGORY_TAWHID = "category_tawhid";
    public static final String CATEGORY_DAILY_DUAS = "category_daily_duas";

    private static final List<DhikrItem> ISTIGHFAR = Collections.unmodifiableList(Arrays.asList(
            new DhikrItem(
                    "sayyid_istighfar",
                    "سید الاستغفار",
                    "اللّٰهُمَّ أَنْتَ رَبِّي لَا إِلٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي، فَاغْفِرْ لِي، فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
                    "اے اللہ! تو ہی میرا رب ہے، تیرے سوا کوئی معبود نہیں۔ تو نے مجھے پیدا کیا اور میں تیرا بندہ ہوں۔ میں اپنی استطاعت کے مطابق تیرے عہد اور وعدے پر قائم ہوں۔ میں اپنے کیے کے شر سے تیری پناہ مانگتا ہوں۔ میں اپنے اوپر تیری نعمت کا اقرار کرتا ہوں اور اپنے گناہ کا اعتراف کرتا ہوں، پس مجھے بخش دے، کیونکہ تیرے سوا کوئی گناہوں کو نہیں بخشتا۔",
                    1,
                    true,
                    true,
                    "Sahih al-Bukhari 6306. Verify text and translation before public release."),
            new DhikrItem(
                    MainActivity.MODE_ISTIGHFAR,
                    "استغفار اور توبہ",
                    "أَسْتَغْفِرُ اللّٰهَ وَأَتُوبُ إِلَيْهِ",
                    "میں اللہ سے بخشش مانگتا ہوں اور اسی کی طرف توبہ کرتا ہوں۔",
                    100,
                    true,
                    false,
                    "Sahih Muslim 2702 / Hisn al-Muslim 96. Verify before public release."),
            new DhikrItem(
                    "rabbighfirli",
                    "رَبِّ اغْفِرْ لِي وَتُبْ عَلَيَّ",
                    "رَبِّ اغْفِرْ لِي وَتُبْ عَلَيَّ، إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ",
                    "اے میرے رب! مجھے بخش دے اور میری توبہ قبول فرما، بے شک تو بہت توبہ قبول کرنے والا، نہایت رحم والا ہے۔",
                    100,
                    true,
                    false,
                    "Abu Dawud and At-Tirmidhi; verify exact numbering before public release."),
            new DhikrItem(
                    "hayy_qayyum_istighfar",
                    "استغفارِ حی و قیوم",
                    "أَسْتَغْفِرُ اللّٰهَ الْعَظِيمَ الَّذِي لَا إِلٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ",
                    "میں عظمت والے اللہ سے بخشش مانگتا ہوں، جس کے سوا کوئی معبود نہیں، جو ہمیشہ زندہ اور سب کو قائم رکھنے والا ہے، اور میں اسی کی طرف توبہ کرتا ہوں۔",
                    1,
                    false,
                    true,
                    "Abu Dawud, At-Tirmidhi and Al-Hakim. No fixed count stored; verify before public release.")
    ));

    private static final List<DhikrItem> TASBEEH = Collections.unmodifiableList(Arrays.asList(
            new DhikrItem(
                    MainActivity.MODE_SUBHAN_BIHAMDIHI,
                    "سبحان اللہ وبحمدہ",
                    "سُبْحَانَ اللّٰهِ وَبِحَمْدِهِ",
                    "اللہ ہر عیب سے پاک ہے اور تمام تعریف اسی کے لیے ہے۔",
                    100,
                    true,
                    false,
                    "Sahih al-Bukhari 6405. Verify before public release."),
            new DhikrItem(
                    "subhan_bihamdihi_azim",
                    "دو محبوب کلمات",
                    "سُبْحَانَ اللّٰهِ وَبِحَمْدِهِ، سُبْحَانَ اللّٰهِ الْعَظِيمِ",
                    "اللہ ہر عیب سے پاک ہے اور تمام تعریف اسی کے لیے ہے؛ عظمت والا اللہ ہر عیب سے پاک ہے۔",
                    1,
                    false,
                    false,
                    "Sahih al-Bukhari and Sahih Muslim. No fixed count stored; verify before public release.")
    ));

    private static final String[] FIVE_TAHLILAT_PHRASES = new String[]{
            "لَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ",
            "لَا إِلٰهَ إِلَّا اللّٰهُ وَحْدَهُ",
            "لَا إِلٰهَ إِلَّا اللّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            "لَا إِلٰهَ إِلَّا اللّٰهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ",
            "لَا إِلٰهَ إِلَّا اللّٰهُ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ"
    };

    private static final String[] FIVE_TAHLILAT_TRANSLATIONS = new String[]{
            "اللہ کے سوا کوئی معبود نہیں اور اللہ سب سے بڑا ہے۔",
            "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے۔",
            "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں۔",
            "اللہ کے سوا کوئی معبود نہیں؛ بادشاہی اسی کی ہے اور تمام تعریف اسی کے لیے ہے۔",
            "اللہ کے سوا کوئی معبود نہیں، اور گناہ سے بچنے اور نیکی کرنے کی طاقت اللہ ہی سے ہے۔"
    };

    private static final List<DhikrItem> TAWHID = Collections.unmodifiableList(Arrays.asList(
            new DhikrItem(
                    "five_tahlilat",
                    "پانچ تہلیلات",
                    "لَا إِلٰهَ إِلَّا اللّٰهُ — پانچ مبارک کلمات",
                    "اللہ کی وحدانیت، بڑائی، بادشاہی، حمد اور اسی پر کامل بھروسا بیان کرنے والے پانچ کلمات۔",
                    "ہر کلمے سے متعلق حدیثی اضافہ: اللہ تعالیٰ کی تصدیق\n\n"
                            + "1۔ بندہ کہتا ہے: لَا إِلٰهَ إِلَّا اللّٰهُ وَاللّٰهُ أَكْبَرُ\n"
                            + "جواب: میرے بندے نے سچ کہا؛ میرے سوا کوئی معبود نہیں اور میں سب سے بڑا ہوں۔\n\n"
                            + "2۔ بندہ کہتا ہے: لَا إِلٰهَ إِلَّا اللّٰهُ وَحْدَهُ\n"
                            + "جواب: میرے بندے نے سچ کہا؛ میرے سوا کوئی معبود نہیں، میں اکیلا ہوں۔\n\n"
                            + "3۔ بندہ کہتا ہے: لَا إِلٰهَ إِلَّا اللّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ\n"
                            + "جواب: میرے بندے نے سچ کہا؛ میرے سوا کوئی معبود نہیں اور میرا کوئی شریک نہیں۔\n\n"
                            + "4۔ بندہ کہتا ہے: لَا إِلٰهَ إِلَّا اللّٰهُ لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ\n"
                            + "جواب: میرے بندے نے سچ کہا؛ بادشاہی میری ہے اور تعریف میرے ہی لیے ہے۔\n\n"
                            + "5۔ بندہ کہتا ہے: لَا إِلٰهَ إِلَّا اللّٰهُ وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ\n"
                            + "جواب: میرے بندے نے سچ کہا؛ میرے سوا کوئی معبود نہیں اور طاقت و قوت صرف میری مدد سے ہے۔",
                    1,
                    true,
                    true,
                    "Sunan Ibn Majah 3794. Related hadith addition; review wording, variants and grading before public release.",
                    FIVE_TAHLILAT_PHRASES,
                    FIVE_TAHLILAT_TRANSLATIONS,
                    new int[]{1, 1, 1, 1, 1}),
            new DhikrItem(
                    MainActivity.MODE_TAWHID,
                    "کلمۂ توحید",
                    "لَا إِلٰهَ إِلَّا اللّٰهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
                    "اللہ کے سوا کوئی معبود نہیں، وہ اکیلا ہے، اس کا کوئی شریک نہیں۔ اسی کے لیے بادشاہی اور اسی کے لیے تمام تعریف ہے، اور وہ ہر چیز پر پوری قدرت رکھتا ہے۔",
                    100,
                    true,
                    true,
                    "Sahih al-Bukhari / Sahih Muslim. Verify exact numbering before public release."),
            new DhikrItem(
                    "la_ilaha_illallah",
                    "لَا إِلٰهَ إِلَّا اللّٰهُ",
                    "لَا إِلٰهَ إِلَّا اللّٰهُ",
                    "اللہ کے سوا کوئی معبود نہیں۔",
                    100,
                    false,
                    false,
                    "General dhikr. App stores a personal starter target, not a reported fixed count.")
    ));

    private static final List<DhikrItem> DAILY_DUAS = Collections.unmodifiableList(Arrays.asList(
            new DhikrItem(
                    "leaving_home_dua",
                    "گھر سے نکلنے کی دعا",
                    "بِسْمِ اللّٰهِ، تَوَكَّلْتُ عَلَى اللّٰهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللّٰهِ",
                    "اللہ کے نام کے ساتھ نکلتا ہوں، میں نے اللہ پر بھروسا کیا، اور گناہ سے بچنے اور نیکی کرنے کی طاقت اللہ ہی کی مدد سے ہے۔",
                    "وضاحت\n\n"
                            + "جب اللہ کا بندہ بسم اللہ کہتا ہے تو فرشتے جواب دیتے ہیں: تمہیں ہدایت دی گئی۔\n\n"
                            + "جب وہ توکلت علی اللہ کہتا ہے تو فرشتے جواب دیتے ہیں: تمہارے لیے کفایت کردی گئی۔\n\n"
                            + "اور جب وہ لا حول ولا قوة إلا بالله کہتا ہے تو فرشتے جواب دیتے ہیں: تمہیں حفاظت دے دی گئی۔\n\n"
                            + "جب بندے کی یہ دعا شیطان سنتے ہیں تو وہ دوسرے شیطان سے کہتا ہے: تم ایسے شخص کا کیا بگاڑ سکتے ہو جسے ہدایت، کفایت اور حفاظت دے دی گئی ہو؟",
                    1,
                    true,
                    true,
                    "Sunan Abi Dawud 5095 / Riyad as-Salihin 83. The teaching presentation maps the three responses to the three clauses; the hadith reports them after the complete dua. Keep hidden until reviewed for public release.")
    ));

    private DhikrCatalog() {}

    public static List<DhikrItem> getItems(String category) {
        if (CATEGORY_ISTIGHFAR.equals(category)) return ISTIGHFAR;
        if (CATEGORY_TASBEEH.equals(category)) return TASBEEH;
        if (CATEGORY_TAWHID.equals(category)) return TAWHID;
        if (CATEGORY_DAILY_DUAS.equals(category)) return DAILY_DUAS;
        return Collections.emptyList();
    }

    public static List<DhikrItem> getAllItems() {
        List<DhikrItem> all = new ArrayList<>();
        all.addAll(ISTIGHFAR);
        all.addAll(TASBEEH);
        all.addAll(TAWHID);
        all.addAll(DAILY_DUAS);
        return Collections.unmodifiableList(all);
    }

    public static DhikrItem findById(String id) {
        if (id == null) return null;
        for (DhikrItem item : getAllItems()) {
            if (id.equals(item.id)) return item;
        }
        return null;
    }

    public static String getCategoryTitle(String category) {
        if (CATEGORY_ISTIGHFAR.equals(category)) return "استغفار";
        if (CATEGORY_TASBEEH.equals(category)) return "تسبیح و تحمید";
        if (CATEGORY_TAWHID.equals(category)) return "توحید کے اذکار";
        if (CATEGORY_DAILY_DUAS.equals(category)) return "روزمرہ دعائیں";
        return "اذکار";
    }

    public static String getCategorySubtitle(String category) {
        if (CATEGORY_ISTIGHFAR.equals(category)) return "سید الاستغفار اور بخشش و توبہ کے مسنون الفاظ";
        if (CATEGORY_TASBEEH.equals(category)) return "سبحان اللہ سے شروع ہونے والے منتخب اذکار";
        if (CATEGORY_TAWHID.equals(category)) return "اللہ کی وحدانیت کے منتخب کلمات اور پانچ تہلیلات";
        if (CATEGORY_DAILY_DUAS.equals(category)) return "روزمرہ مواقع کی دعائیں، ترجمہ اور دل نشین تفہیم";
        return "ذکر منتخب کریں";
    }
}
