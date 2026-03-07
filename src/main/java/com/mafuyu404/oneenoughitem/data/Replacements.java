package com.mafuyu404.oneenoughitem.data;

import com.mafuyu404.oelib.api.DataDriven;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

@DataDriven(
        modid = "oneenoughitem",
        folder = "replacements",
        syncToClient =false,        //暂时关闭客户端同步，目前前置库 OELib 的网络同步问题无法解决
        validator = ReplacementValidator.class,
        supportArray = true
)
public record Replacements(List<String> matchItems, String resultItems) {
    public static final Codec<Replacements> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.listOf().fieldOf("matchItems").forGetter(Replacements::matchItems),
                    Codec.STRING.fieldOf("resultItems").forGetter(Replacements::resultItems)
            ).apply(instance, Replacements::new)
    );
}