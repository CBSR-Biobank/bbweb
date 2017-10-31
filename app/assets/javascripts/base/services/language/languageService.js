/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
class LanguageService {

  constructor($window, gettextCatalog) {
    'ngInject'
    Object.assign(this, { $window, gettextCatalog });
  }

  initLanguage(options) {
    options = options || {};
    var lang = this.$window.localStorage.getItem('biobankAppLang') || 'en';
    this.gettextCatalog.setCurrentLanguage(lang);
    if (lang !== 'en') {
      this.gettextCatalog.loadRemote('/assets/languages/' + lang + '.json');
    }

    if (options.debug) {
      this.gettextCatalog.debug = options.debug;
    }
  }

  setLanguage(lang) {
    this.gettextCatalog.setCurrentLanguage(lang);
    if (lang !== 'en') {
      this.gettextCatalog.loadRemote('/assets/languages/' + lang + '.json');
    }
    this.$window.localStorage.setItem('biobankAppLang', lang);
  }
}

export default ngModule => ngModule.service('languageService', LanguageService)
