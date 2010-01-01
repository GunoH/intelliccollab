package com.quinity.ccollab.intellij.ui;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.util.Consumer;
import com.sun.net.ssl.internal.ssl.SSLEngineImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;

public class SpellCheckerManager {

  private static final Logger LOG = Logger.getInstance("#com.intellij.spellchecker.SpellCheckerManager");

  private static final int MAX_SUGGESTIONS_THRESHOLD = 5;
  private static final int MAX_METRICS = 1;

  private Project project;

//  private SpellCheckerEngine spellChecker;

  private Dictionary userDictionary;



//  @NotNull
//  private final SuggestionProvider suggestionProvider = new BaseSuggestionProvider(this);

  private final SpellCheckerSettings settings;

  public static SpellCheckerManager getInstance(Project project) {
    return ServiceManager.getService(project, SpellCheckerManager.class);
  }

  public SpellCheckerManager(Project project, SpellCheckerSettings settings) {
    this.project = project;
    this.settings = settings;
    reloadConfiguration();

  }

  public Project getProject() {
    return project;
  }

  public Dictionary getUserDictionary() {
    return userDictionary;
  }

  public void reloadConfiguration() {
//    spellChecker = SpellCheckerFactory.create();
    fillEngineDictionary();
  }

  private void fillEngineDictionary() {
//    spellChecker.reset();
//    final StateLoader stateLoader = new StateLoader(project);
//    final List<Loader> loaders = new ArrayList<Loader>();
    // Load bundled dictionaries from corresponding jars
//    for (BundledDictionaryProvider provider : Extensions.getExtensions(BundledDictionaryProvider.EP_NAME)) {
//      for (String dictionary : provider.getBundledDictionaries()) {
//        if (this.settings == null || !this.settings.getBundledDisabledDictionariesPaths().contains(dictionary)) {
//          final Class<? extends BundledDictionaryProvider> loaderClass = provider.getClass();
//          final InputStream stream = loaderClass.getResourceAsStream(dictionary);
//          if (stream != null){
//            loaders.add(new StreamLoader(stream));
//          } else {
//            LOG.warn("Couldn't load dictionary '" + dictionary + "' for loader '" + loaderClass + "'");
//          }
//        }
//      }
//    }
    if (this.settings != null && this.settings.getDictionaryFoldersPaths() != null) {
      final Set<String> disabledDictionaries = settings.getDisabledDictionariesPaths();
      for (String folder : this.settings.getDictionaryFoldersPaths()) {
/*
        SPFileUtil.processFilesRecursively(folder, new Consumer<String>() {
          public void consume(final String s) {
            if (!disabledDictionaries.contains(s)) {
              loaders.add(new FileLoader(s));
            }
          }
        });
*/

      }
    }
//    loaders.add(stateLoader);
//    for (Loader loader : loaders) {
//      spellChecker.loadDictionary(loader);
//    }
//    userDictionary = stateLoader.getDictionary();
  }


/*
  public boolean hasProblem(@NotNull String word) {
    return !spellChecker.isCorrect(word);
  }
*/

/*
  public void acceptWordAsCorrect(@NotNull String word) {
    final String transformed = spellChecker.getTransformation().transform(word);
    if (transformed != null) {
      userDictionary.addToDictionary(transformed);
      spellChecker.addToDictionary(transformed);
    }
  }
*/

  public void update(@Nullable Collection<String> words, SpellCheckerSettings allDictionaries) {
//    userDictionary.replaceAll(words);
    reloadConfiguration();
    restartInspections();
  }


  @NotNull
  public List<String> getBundledDictionaries() {
    final ArrayList<String> dictionaries = new ArrayList<String>();
//    for (BundledDictionaryProvider provider : Extensions.getExtensions(BundledDictionaryProvider.EP_NAME)) {
//      dictionaries.addAll(Arrays.asList(provider.getBundledDictionaries()));
//    }
    return dictionaries;
  }

//  @NotNull
//  public static HighlightDisplayLevel getHighlightDisplayLevel() {
//    return HighlightDisplayLevel.find(SpellCheckerSeveritiesProvider.TYPO);
//  }

/*
  @NotNull
  public List<String> getSuggestions(@NotNull String text) {
    return suggestionProvider.getSuggestions(text);
  }
*/


  
/*
  @NotNull
  protected List<String> getRawSuggestions(@NotNull String word) {
    if (!spellChecker.isCorrect(word)) {
      List<String> suggestions = spellChecker.getSuggestions(word, MAX_SUGGESTIONS_THRESHOLD, MAX_METRICS);
      if (suggestions.size() != 0) {
        boolean capitalized = Strings.isCapitalized(word);
        boolean upperCases = Strings.isUpperCase(word);
        if (capitalized) {
          Strings.capitalize(suggestions);
        }
        else if (upperCases) {
          Strings.upperCase(suggestions);
        }
      }
      List<String> result = new ArrayList<String>();
      for (String s : suggestions) {
        if (!result.contains(s)) {
          result.add(s);
        }
      }
      return result;
    }
    return Collections.emptyList();
  }
*/

  @NotNull
  public List<String> getVariants(@NotNull String prefix) {

    return Collections.emptyList();
  }


  public void restartInspections() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : projects) {
          if (project.isInitialized() && project.isOpen() && !project.isDefault()) {
            DaemonCodeAnalyzer.getInstance(project).restart();
          }
        }
      }
    });
  }


}