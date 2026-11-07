package com.example.dietapp.ui.components

import androidx.annotation.DrawableRes
import com.example.dietapp.R
import java.text.Normalizer

private data class FoodIconEntry(
    val foodCode: String,
    val foodName: String,
    @DrawableRes val drawable: Int,
)

private val foodIconEntries = listOf(
    // 밥류
    FoodIconEntry("D301-022000000-0001", "쌀밥", R.drawable.ic_bori_food_rice),
    FoodIconEntry("D101-050000000-0001", "현미밥", R.drawable.ic_bori_food_brown_rice),
    FoodIconEntry("D101-032000000-0001", "잡곡밥", R.drawable.ic_bori_food_multigrain_rice),
    FoodIconEntry("D301-017070000-0002", "김치볶음밥", R.drawable.ic_bori_food_kimchi_fried_rice),
    FoodIconEntry("D301-017260000-0001", "새우볶음밥", R.drawable.ic_bori_food_shrimp_fried_rice),
    FoodIconEntry("D301-018000000-0002", "비빔밥", R.drawable.ic_bori_food_bibimbap),
    FoodIconEntry("D101-028000000-0001", "오므라이스", R.drawable.ic_bori_food_omurice),
    FoodIconEntry("D101-044000000-0001", "카레라이스", R.drawable.ic_bori_food_curry_rice),
    FoodIconEntry("D101-007000000-0001", "김밥", R.drawable.ic_bori_food_gimbap),
    FoodIconEntry("D301-007450000-0002", "참치김밥", R.drawable.ic_bori_food_tuna_gimbap),
    FoodIconEntry("D301-019460000-0001", "참치마요 삼각김밥", R.drawable.ic_bori_food_triangle_gimbap),
    FoodIconEntry("D301-042410000-0001", "유부초밥", R.drawable.ic_bori_food_inari_sushi),
    FoodIconEntry("D101-010150000-0001", "제육덮밥", R.drawable.ic_bori_food_pork_rice),
    FoodIconEntry("D101-010240000-0001", "불고기덮밥", R.drawable.ic_bori_food_beef_rice),
    FoodIconEntry("D301-036390000-0001", "짜장밥", R.drawable.ic_bori_food_jjajang_rice),
    FoodIconEntry("D301-042200000-0001", "모둠초밥", R.drawable.ic_bori_food_sushi),

    // 면·만두류
    FoodIconEntry("D103-148000000-0001", "라면", R.drawable.ic_bori_food_ramen),
    FoodIconEntry("D703-197000000-0001", "짜장면", R.drawable.ic_bori_food_jjajang_noodles),
    FoodIconEntry("D103-172000000-0001", "짬뽕", R.drawable.ic_bori_food_jjamppong),
    FoodIconEntry("D303-164400000-0001", "우동", R.drawable.ic_bori_food_udon),
    FoodIconEntry("D103-142410000-0001", "잔치국수", R.drawable.ic_bori_food_janchi),
    FoodIconEntry("D303-157000000-0001", "비빔국수", R.drawable.ic_bori_food_bibim_noodles),
    FoodIconEntry("D303-144180000-0001", "물냉면", R.drawable.ic_bori_food_mul_naengmyeon),
    FoodIconEntry("D303-144280000-0001", "비빔냉면", R.drawable.ic_bori_food_bibim_naengmyeon),
    FoodIconEntry("D103-174000000-0001", "칼국수", R.drawable.ic_bori_food_kalguksu),
    FoodIconEntry("D103-173000000-0001", "쫄면", R.drawable.ic_bori_food_jjolmyeon),
    FoodIconEntry("D303-161490000-0001", "토마토 스파게티", R.drawable.ic_bori_food_tomato_pasta),
    FoodIconEntry("D303-161480000-0001", "크림 스파게티", R.drawable.ic_bori_food_cream_pasta),
    FoodIconEntry("D110-468000000-0001", "라볶이", R.drawable.ic_bori_food_rabokki),
    FoodIconEntry("D303-162000000-0001", "쌀국수", R.drawable.ic_bori_food_pho),
    FoodIconEntry("D103-150030000-0001", "군만두", R.drawable.ic_bori_food_fried_dumplings),
    FoodIconEntry("D103-150190000-0001", "물만두", R.drawable.ic_bori_food_water_dumplings),
    FoodIconEntry("D303-150010000-0001", "고기만두(찐만두)", R.drawable.ic_bori_food_steamed_dumplings),

    // 국·찌개·탕류
    FoodIconEntry("D306-266000000-0001", "김치찌개", R.drawable.ic_bori_food_kimchi_stew),
    FoodIconEntry("D106-275000000-0001", "된장찌개", R.drawable.ic_bori_food_doenjang_stew),
    FoodIconEntry("D106-291030000-0001", "김치순두부찌개", R.drawable.ic_bori_food_sundubu),
    FoodIconEntry("D306-284000000-0004", "부대찌개", R.drawable.ic_bori_food_budae_stew),
    FoodIconEntry("D105-223000000-0001", "미역국", R.drawable.ic_bori_food_miyeok_soup),
    FoodIconEntry("D105-253000000-0001", "콩나물국", R.drawable.ic_bori_food_bean_sprout_soup),
    FoodIconEntry("D305-219240000-0001", "소고기무국", R.drawable.ic_bori_food_beef_radish_soup),
    FoodIconEntry("D105-245000000-0001", "육개장", R.drawable.ic_bori_food_yukgaejang),
    FoodIconEntry("D105-199000000-0001", "갈비탕", R.drawable.ic_bori_food_galbitang),
    FoodIconEntry("D105-231000000-0001", "설렁탕", R.drawable.ic_bori_food_seolleongtang),
    FoodIconEntry("D106-260000000-0001", "감자탕", R.drawable.ic_bori_food_gamjatang),
    FoodIconEntry("D105-237000000-0001", "어묵탕", R.drawable.ic_bori_food_fishcake_soup),
    FoodIconEntry("D103-145000000-0001", "떡국", R.drawable.ic_bori_food_tteokguk),
    FoodIconEntry("D101-004310000-0001", "순대국밥", R.drawable.ic_bori_food_sundae_soup),
    FoodIconEntry("D306-278000000-0002", "마라탕", R.drawable.ic_bori_food_mala),
    FoodIconEntry("D101-037000000-0001", "짬뽕밥", R.drawable.ic_bori_food_jjamppong_rice),
    FoodIconEntry("D105-228000000-0001", "삼계탕", R.drawable.ic_bori_food_samgyetang),

    // 고기·생선 주찬
    FoodIconEntry("D110-465000000-0001", "제육볶음", R.drawable.ic_bori_food_jeyuk),
    FoodIconEntry("D310-488000000-0001", "오징어볶음", R.drawable.ic_bori_food_squid_stirfry),
    FoodIconEntry("D310-462000000-0004", "닭갈비", R.drawable.ic_bori_food_dakgalbi),
    FoodIconEntry("D110-467000000-0001", "떡볶이", R.drawable.ic_bori_food_tteokbokki),
    FoodIconEntry("D307-337000000-0001", "순대", R.drawable.ic_bori_food_sundae),
    FoodIconEntry("D110-457000000-0001", "낙지볶음", R.drawable.ic_bori_food_nakji),
    FoodIconEntry("D310-493000000-0001", "주꾸미볶음", R.drawable.ic_bori_food_jjukkumi),
    FoodIconEntry("D308-386000000-0001", "소불고기", R.drawable.ic_bori_food_bulgogi),
    FoodIconEntry("D108-382000000-0001", "삼겹살구이", R.drawable.ic_bori_food_pork_belly),
    FoodIconEntry("D108-357110000-0001", "고등어구이", R.drawable.ic_bori_food_mackerel),
    FoodIconEntry("D308-388000000-0001", "연어구이", R.drawable.ic_bori_food_salmon),
    FoodIconEntry("D307-344000000-0001", "족발", R.drawable.ic_bori_food_jokbal),
    FoodIconEntry("D107-324000000-0001", "돼지갈비찜", R.drawable.ic_bori_food_galbijjim),
    FoodIconEntry("D307-318140000-0001", "안동찜닭", R.drawable.ic_bori_food_jjimdak),
    FoodIconEntry("D110-455160000-0001", "돼지곱창볶음", R.drawable.ic_bori_food_gopchang),
    FoodIconEntry("D310-482000000-0001", "소시지볶음", R.drawable.ic_bori_food_sausage_stirfry),
    FoodIconEntry("D311-532081900-0001", "소고기 메추리알 장조림", R.drawable.ic_bori_food_beef_quail_eggs),
    FoodIconEntry("D111-517000000-0001", "두부조림", R.drawable.ic_bori_food_braised_tofu),

    // 튀김류
    FoodIconEntry("D312-550000000-0002", "돈가스", R.drawable.ic_bori_food_tonkatsu),
    FoodIconEntry("D312-549000000-0001", "후라이드치킨", R.drawable.ic_bori_food_fried_chicken),
    FoodIconEntry("D712-566000000-0001", "탕수육", R.drawable.ic_bori_food_tangsuyuk),
    FoodIconEntry("D112-537000000-0001", "감자튀김", R.drawable.ic_bori_food_fries),
    FoodIconEntry("D312-541000000-0001", "김말이튀김", R.drawable.ic_bori_food_seaweed_fritters),
    FoodIconEntry("D312-539000000-0001", "고구마튀김", R.drawable.ic_bori_food_sweet_potato_fritters),
    FoodIconEntry("D312-549150000-0001", "치킨너겟", R.drawable.ic_bori_food_nuggets),

    // 반찬·김치
    FoodIconEntry("D109-416000000-0001", "계란말이", R.drawable.ic_bori_food_omelet_roll),
    FoodIconEntry("D307-317040000-0001", "계란찜", R.drawable.ic_bori_food_steamed_egg),
    FoodIconEntry("D109-412000000-0001", "김치전", R.drawable.ic_bori_food_kimchi_pancake),
    FoodIconEntry("D109-441110000-0001", "해물파전", R.drawable.ic_bori_food_seafood_pancake),
    FoodIconEntry("D110-486000000-0001", "어묵볶음", R.drawable.ic_bori_food_eomuk_stirfry),
    FoodIconEntry("D110-472000000-0001", "멸치볶음", R.drawable.ic_bori_food_anchovies),
    FoodIconEntry("D111-508000000-0001", "감자조림", R.drawable.ic_bori_food_braised_potatoes),
    FoodIconEntry("D113-586000000-0001", "시금치나물", R.drawable.ic_bori_food_spinach),
    FoodIconEntry("D113-597000000-0001", "콩나물무침", R.drawable.ic_bori_food_bean_sprouts),
    FoodIconEntry("D315-670000000-0001", "배추김치", R.drawable.ic_bori_food_cabbage_kimchi),
    FoodIconEntry("D315-665000000-0001", "깍두기", R.drawable.ic_bori_food_kkakdugi),
    FoodIconEntry("D114-640080000-0001", "닭가슴살 샐러드", R.drawable.ic_bori_food_chicken_salad),

    // 빵·간식
    FoodIconEntry("D302-123000000-0001", "햄버거", R.drawable.ic_bori_food_hamburger),
    FoodIconEntry("D302-120410000-0001", "페퍼로니 피자", R.drawable.ic_bori_food_pepperoni_pizza),
    FoodIconEntry("D102-096431100-0001", "햄에그 샌드위치", R.drawable.ic_bori_food_sandwich),
    FoodIconEntry("D102-100000000-0001", "식빵", R.drawable.ic_bori_food_bread),
    FoodIconEntry("D102-095000000-0001", "베이글", R.drawable.ic_bori_food_bagel),
    FoodIconEntry("D102-111000000-0001", "크로와상", R.drawable.ic_bori_food_croissant),
    FoodIconEntry("D102-082120000-0001", "링도넛", R.drawable.ic_bori_food_donut),
    FoodIconEntry("D302-110330000-0001", "치즈케이크", R.drawable.ic_bori_food_cheesecake),
    FoodIconEntry("D302-122000000-0001", "핫도그", R.drawable.ic_bori_food_corn_dog),

    // 음료
    FoodIconEntry("D320-748080000-0001", "아메리카노", R.drawable.ic_bori_food_americano),
    FoodIconEntry("D320-748110000-0001", "카페라떼", R.drawable.ic_bori_food_caffe_latte),
    FoodIconEntry("D320-723050000-0001", "바닐라라떼", R.drawable.ic_bori_food_vanilla_latte),
    FoodIconEntry("D320-716042200-0001", "딸기바나나 주스", R.drawable.ic_bori_food_strawberry_banana),
)

private val foodIconsByCode = foodIconEntries.associate { it.foodCode to it.drawable }
private val foodIconsByName = foodIconEntries.associate { normalizedFoodName(it.foodName) to it.drawable }

@DrawableRes
internal fun catalogFoodIconResource(foodCode: String, foodName: String): Int? =
    foodIconsByCode[foodCode.trim()] ?: foodIconsByName[normalizedFoodName(foodName)]

private fun normalizedFoodName(value: String): String =
    Normalizer.normalize(value.trim(), Normalizer.Form.NFC)
