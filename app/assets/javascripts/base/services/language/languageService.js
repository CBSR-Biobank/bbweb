/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * An AngularJS Service that handles the language used by the application to display information to the user.
 *
 * @memberOf base.services
 */
class LanguageService {

  constructor($window, gettextCatalog) {
    'ngInject'
    Object.assign(this, { $window, gettextCatalog });
  }

  /**
   * Used to initialize the language used by the application.
   *
   * @param {object} options - various options used by this function.
   *
   * @param {boolean} options.debug - displays the text that has not been translated to the selected language,
   * if any. Used to debug which text still requires translation.
   */
  initLanguage(options = {}) {
    var lang = this.$window.localStorage.getItem('biobankAppLang') || 'en';
    this.gettextCatalog.setCurrentLanguage(lang);
    if (lang !== 'en') {
      this.gettextCatalog.loadRemote('/assets/languages/' + lang + '.json');
    }

    if (options.debug) {
      this.gettextCatalog.debug = options.debug;
    }
  }

  /**
   * Changes the language used by the application.
   *
   * @param {string} lang - the ISO 639-2 language code of the language to change to.
   */
  setLanguage(lang) {
    this.gettextCatalog.setCurrentLanguage(lang);
    if (lang !== 'en') {
      this.gettextCatalog.loadRemote('/assets/languages/' + lang + '.json');
    }
    this.$window.localStorage.setItem('biobankAppLang', lang);
  }
}

export default ngModule => ngModule.service('languageService', LanguageService)
