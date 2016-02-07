/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'faker',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, faker, testUtils) {
  'use strict';

  /**
   *
   */
  describe('Directive: truncateToggle', function() {
    var rootScope, compile, element,
        textEmptyWarning = 'text not entered yet.';

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, $templateCache) {
      rootScope = $rootScope;
      compile = $compile;

      testUtils.putHtmlTemplates($templateCache,
                               '/assets/javascripts/common/directives/truncateToggle.html');

      element = angular.element(
        '<truncate-toggle' +
          '   text="model.text"' +
          '   toggle-length="model.toggleLength"' +
          '   text-empty-warning="' + textEmptyWarning + '">' +
          '</truncate-toggle>');
    }));

    function createScope(text, toggleLength) {
      var scope = rootScope;

      scope.model = {
        text:         text,
        toggleLength: toggleLength
      };

      compile(element)(scope);
      scope.$digest();
      return scope;
    }

    it('pressing the button truncates the string', function() {
      var pars,
          buttons,
          text = faker.lorem.paragraphs(1),
          scope = createScope(text, 20);

      pars = element.find('p');
      expect(pars.length).toBe(1);
      expect(pars.eq(0).text()).toBe(scope.model.text);

      buttons = element.find('button');
      expect(buttons.length).toBe(1);
      buttons.eq(0).click();
      expect(pars.eq(0).text().length).toBe(scope.model.toggleLength);
    });

    it('pressing the button twice displays whole string', function() {
      var pars,
          buttons,
          text = faker.lorem.paragraphs(1),
          scope = createScope(text, 20);

      pars = element.find('p');
      expect(pars.length).toBe(1);
      expect(pars.eq(0).text()).toBe(scope.model.text);

      buttons = element.find('button');
      expect(buttons.length).toBe(1);
      buttons.eq(0).click();
      buttons.eq(0).click();
      expect(pars.eq(0).text().trim().length).toBe(scope.model.text.length);
    });

    it('button is labelled correctly', function() {
      var buttons,
          text = faker.lorem.paragraphs(1);

      createScope(text, 20);
      buttons = element.find('button');
      expect(buttons.length).toBe(1);
      expect(buttons.eq(0).text().trim()).toBe('Collapse');
    });

    it('if text is null then warning message is displayed', function() {
      var pars,
          text = '';

      createScope(text, 20);
      pars = element.find('p');
      expect(pars.length).toBe(2);
      expect(pars.eq(1).text().trim()).toBe(textEmptyWarning);
    });

  });

});
