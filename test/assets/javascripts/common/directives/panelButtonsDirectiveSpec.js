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
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  /**
   * TODO: not sure how to test open / closed state of the panel since it is a ui-bootstrap panel.
   */
  describe('Directive: panelButtons', function() {
    var modelFuncNames = ['add', 'panelToggle'];

    var createScope = function (options) {
      var self = this;

      self.scope = self.$rootScope.$new();

      options = options || {};

      self.scope.model = {
        add:         function () {},
        addEnabled:  options.addEnabled || false,
        panelOpen:   true,
        panelToggle: function () {}
      };

      _.each(modelFuncNames, function (funcName){
        spyOn(self.scope.model, funcName).and.returnValue(funcName);
      });

      self.$compile(self.element)(self.scope);
      self.scope.$digest();
    };

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (testSuiteMixin, testUtils) {
      _.extend(this, testSuiteMixin);

      this.injectDependencies('$rootScope', '$compile');

      this.putHtmlTemplates(
        '/assets/javascripts/common/directives/panelButtons.html');

      this.element = angular.element(
        '<panel-buttons on-add="model.add()"' +
          '         add-button-title="add location"' +
          '         add-button-enabled="model.addEnabled"' +
          '         panel-open="model.panelOpen">' +
          '</panel-buttons>');
    }));

    it('clicking on a button invokes corresponding function', function() {
      var buttons;

      createScope.call(this, { addEnabled: true });

      buttons = this.element.find('button');
      expect(buttons.length).toBe(2);
      buttons.eq(0).click();
      expect(this.scope.model.add).toHaveBeenCalled();
    });

    it('button not present if disabled', function() {
      var buttons;

      createScope.call(this, { addEnabled: false });
      buttons = this.element.find('button');
      expect(buttons.length).toBe(1);
    });

    it('add button has the correct icon', function() {
      var icons;

      createScope.call(this, { addEnabled: true });
      icons = this.element.find('button i');
      expect(icons.length).toBe(2);
      expect(icons.eq(0)).toHaveClass('glyphicon-plus');
    });

  });

});
