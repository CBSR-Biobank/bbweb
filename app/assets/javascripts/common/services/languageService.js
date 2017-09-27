/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
export default class LanguageService {

  constructor($window, gettextCatalog) {
    this.$window = $window;
    this.gettextCatalog = gettextCatalog;
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

LanguageService.$inject = ['$window', 'gettextCatalog'];
