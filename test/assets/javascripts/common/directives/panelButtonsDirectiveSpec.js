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
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  /**
   * TODO: not sure how to test open / closed state of the panel since it is a ui-bootstrap panel.
   */
  describe('Directive: panelButtons', function() {
    var rootScope, compile, element,
        modelFuncNames = ['add', 'panelToggle'];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, $templateCache) {
      rootScope = $rootScope;
      compile = $compile;

      testUtils.putHtmlTemplates($templateCache,
                               '/assets/javascripts/common/directives/panelButtons.html');

      element = angular.element(
        '<panel-buttons on-add="model.add()"' +
          '         add-button-title="add location"' +
          '         add-button-enabled="model.addEnabled"' +
          '         panel-open="model.panelOpen">' +
          '</panel-buttons>');
    }));

    function createScope(options) {
      var scope = rootScope;

      options = options || {};

      scope.model = {
        add:         function () {},
        addEnabled:  options.addEnabled || false,
        panelOpen:   true,
        panelToggle: function () {}
      };

      _.each(modelFuncNames, function (funcName){
        spyOn(scope.model, funcName).and.returnValue(funcName);
      });

      compile(element)(scope);
      scope.$digest();
      return scope;
    }

    it('clicking on a button invokes corresponding function', function() {
      var buttons, scope = createScope({ addEnabled: true });

      buttons = element.find('button');
      expect(buttons.length).toBe(2);
      buttons.eq(0).click();
      expect(scope.model.add).toHaveBeenCalled();
    });

    it('button not present if disabled', function() {
      var buttons;

      createScope({ addEnabled: false });
      buttons = element.find('button');
      expect(buttons.length).toBe(1);
    });

    it('add button has the correct icon', function() {
      var icons;

      createScope({ addEnabled: true });
      icons = element.find('button i');
      expect(icons.length).toBe(2);
      expect(icons.eq(0)).toHaveClass('glyphicon-plus');
    });

  });

});
