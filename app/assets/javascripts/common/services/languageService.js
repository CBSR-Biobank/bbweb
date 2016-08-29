/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  languageService.$inject = ['$window', 'gettextCatalog'];

  /**
   * Service that allows switching of display language.
   */
  function languageService($window, gettextCatalog) {
    var service = {
      initLanguage: initLanguage,
      setLanguage: setLanguage
    };
    return service;

    //-------

    function initLanguage(options) {
      options = options || {};
      var lang = $window.localStorage.getItem('biobankAppLang') || 'en';
      gettextCatalog.setCurrentLanguage(lang);
      if (lang !== 'en') {
        gettextCatalog.loadRemote('/assets/languages/' + lang + '.json');
      }

      if (options.debug) {
        gettextCatalog.debug = options.debug;
      }
    }

    function setLanguage(lang) {
      gettextCatalog.setCurrentLanguage(lang);
      if (lang !== 'en') {
        gettextCatalog.loadRemote('/assets/languages/' + lang + '.json');
      }
      $window.localStorage.setItem('biobankAppLang', lang);
    }

  }

  return languageService;
});
