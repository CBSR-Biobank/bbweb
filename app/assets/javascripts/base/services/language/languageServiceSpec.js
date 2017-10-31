/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('languageService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);
      this.injectDependencies('$window',
                              'gettextCatalog',
                              'languageService');

      this.$window.localStorage.setItem = jasmine.createSpy().and.returnValue(null);
      this.$window.localStorage.getItem = jasmine.createSpy().and.returnValue('es');
      spyOn(this.gettextCatalog, 'setCurrentLanguage').and.callThrough();
      spyOn(this.gettextCatalog, 'loadRemote').and.callThrough();

    });

  });

  afterEach(function() {
    // this is likely a jasmine bug, spies are not reset on $window.localStorage
    //
    // see https://github.com/jasmine/jasmine/issues/299
    this.$window.localStorage.getItem = jasmine.createSpy().and.returnValue(null);
  });

  describe('when initializing the language', function() {

    it('can initialize the users selected language', function() {
      this.languageService.initLanguage();
      expect(this.gettextCatalog.setCurrentLanguage).toHaveBeenCalledWith('es');
    });

    it('can be initialized with debug setting', function() {
      this.languageService.initLanguage({ debug: true });
      expect(this.gettextCatalog.setCurrentLanguage).toHaveBeenCalledWith('es');
      expect(this.gettextCatalog.debug).toBeTrue();
    });

  });

  describe('when setting the language', function() {

    it('setting language to english does not load a language file', function() {
      this.languageService.setLanguage('en');
      expect(this.gettextCatalog.setCurrentLanguage).toHaveBeenCalledWith('en');
      expect(this.gettextCatalog.loadRemote).not.toHaveBeenCalled();
      expect(this.$window.localStorage.setItem).toHaveBeenCalledWith('biobankAppLang', 'en');
    });

    it('setting language to other than english loads the language file', function() {
      this.languageService.setLanguage('es');
      expect(this.gettextCatalog.setCurrentLanguage).toHaveBeenCalledWith('es');
      expect(this.gettextCatalog.loadRemote).toHaveBeenCalledWith('/assets/languages/es.json');
      expect(this.$window.localStorage.setItem).toHaveBeenCalledWith('biobankAppLang', 'es');
    });

  });

});
