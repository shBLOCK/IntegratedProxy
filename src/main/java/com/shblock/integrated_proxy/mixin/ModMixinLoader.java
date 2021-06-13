package com.shblock.integrated_proxy.mixin;

import com.shblock.integrated_proxy.IntegratedProxy;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModClassLoader;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.MixinProcessor;
import org.spongepowered.asm.mixin.transformer.Proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Mixin(Loader.class)
public class ModMixinLoader {
    @Shadow
    private List<ModContainer> mods;
    @Shadow private ModClassLoader modClassLoader;

    @Inject(method = "loadMods", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/LoadController;transition(Lnet/minecraftforge/fml/common/LoaderState;Z)V", ordinal = 1), remap = false)
    private void beforeConstructingMods(List<String> injectedModContainers, CallbackInfo ci) {
        for (ModContainer mod : mods) {
            try {
                modClassLoader.addFile(mod.getSource());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        MixinEnvironment.getEnvironment(MixinEnvironment.Phase.DEFAULT).addConfiguration("mixins.integrated_proxy.mod.json");
        Logger.getGlobal().log(Level.INFO,"Added mixin config: mixins.integrated_proxy.mod.json");

        Proxy mixinProxy = (Proxy) Launch.classLoader.getTransformers().stream().filter(transformer -> transformer instanceof Proxy).findFirst().get();
        try {
            Class mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");

            Field transformerField = Proxy.class.getDeclaredField("transformer");
            transformerField.setAccessible(true);
            Object transformer = transformerField.get(mixinProxy);

            //Get MixinProcessor from MixinTransformer
            Field processorField = mixinTransformerClass.getDeclaredField("processor");
            processorField.setAccessible(true);
            Object processor = processorField.get(transformer);

            Method selectConfigsMethod = MixinProcessor.class.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
            selectConfigsMethod.setAccessible(true);
            selectConfigsMethod.invoke(processor, MixinEnvironment.getCurrentEnvironment());

            Method prepareConfigsMethod = MixinProcessor.class.getDeclaredMethod("prepareConfigs", MixinEnvironment.class);
            prepareConfigsMethod.setAccessible(true);
            prepareConfigsMethod.invoke(processor, MixinEnvironment.getCurrentEnvironment());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
