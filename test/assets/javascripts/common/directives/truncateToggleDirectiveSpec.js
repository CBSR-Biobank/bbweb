/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'faker',
  'biobankApp'
], function(angular, mocks, _, faker) {
  'use strict';

  describe('Directive: truncateToggle', function() {
    var textEmptyWarning = 'text not entered yet.';

    var createScope = function (text, toggleLength) {
      var self = this;

      self.element = angular.element([
        '<truncate-toggle',
        '   text="model.text"',
        '   toggle-length="model.toggleLength"',
        '   text-empty-warning="' + textEmptyWarning + '">',
        '</truncate-toggle>'
      ].join(''));

      self.scope = self.$rootScope.$new();

      self.scope.model = {
        text:         text,
        toggleLength: toggleLength
      };

      this.$compile(self.element)(self.scope);
      self.scope.$digest();
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (testSuiteMixin, testUtils) {
      var self = this;

      _.extend(self, testSuiteMixin);

      self.injectDependencies('$rootScope', '$compile', '$filter');
      self.putHtmlTemplates(
        '/assets/javascripts/common/directives/truncateToggle.html');
    }));

    it('pressing the button truncates the string', function() {
      var divs,
          buttons,
          text = '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ' +
          '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ';

      createScope.call(this, text, 20);

      divs = angular.element(this.element[0].getElementsByClassName('col-md-12'));
      expect(divs.length).toBe(1);
      expect(divs.eq(0).text()).toBe(text);

      buttons = this.element.find('button');
      expect(buttons.length).toBe(1);
      buttons.eq(0).click();
      expect(divs.eq(0).text().length).toBe(this.scope.model.toggleLength);
    });

    it('pressing the button twice displays whole string', function() {
      var divs,
          buttons,
          text = '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ' +
          '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ';

      createScope.call(this, text, 20);

      divs = angular.element(this.element[0].getElementsByClassName('col-md-12'));
      expect(divs.length).toBe(1);

      buttons = this.element.find('button');
      expect(buttons.length).toBe(1);
      buttons.eq(0).click();
      buttons.eq(0).click();
      expect(divs.eq(0).text()).toBe(text);
    });

    it('button is labelled correctly', function() {
      var buttons,
          text = '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ' +
          '123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ';

      createScope.call(this, text, 20);
      buttons = this.element.find('button');
      expect(buttons.length).toBe(1);
      expect(buttons.eq(0).text().trim()).toBe('Show less');
    });

    it('if text is null then warning message is displayed', function() {
      var divs,
          text = '';

      createScope.call(this, text, 20);
      divs = angular.element(this.element[0].getElementsByClassName('alert'));
      expect(divs.length).toBe(1);
      expect(divs.eq(0).text().trim()).toBe(textEmptyWarning);
    });

  });

});
