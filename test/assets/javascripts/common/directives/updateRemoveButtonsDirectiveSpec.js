/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'jquery',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, $, _, testUtils) {
  'use strict';

  describe('Directive: updateRemoveButtons', function() {
    var rootScope, compile, element,
        buttonClickFuncNames = ['update', 'remove'];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, $templateCache) {
      rootScope = $rootScope;
      compile = $compile;

      testUtils.putHtmlTemplate($templateCache,
                               '/assets/javascripts/common/directives/updateRemoveButtons.html');

      element = angular.element(
        '<update-remove-buttons' +
          '   on-update="model.update()"' +
          '   on-remove="model.remove()"' +
          '   update-button-enabled="model.updateAllowed"' +
          '   remove-button-enabled="model.removeAllowed">' +
          '</update-remove-buttons>');

      testUtils.addCustomMatchers();
    }));

    function createScope(options) {
      var scope = rootScope;

      options = options || {};

      scope.model = {};
      scope.model.updateAllowed = options.updateAllowed || false;
      scope.model.removeAllowed = options.removeAllowed || false;

      _.each(buttonClickFuncNames, function (key){
        scope.model[key] = function () {};
        spyOn(scope.model, key).and.returnValue(key);
      });

      compile(element)(scope);
      scope.$digest();
      return scope;
    }

    it('clicking on a button invokes corresponding function', function() {
      var buttons, scope = createScope({ updateAllowed: true, removeAllowed: true});

      buttons = element.find('button');

      expect(buttons.length).toBe(2);
      _.each(_.range(buttons.length), function (i) {
        buttons.eq(i).click();
        switch (i) {
        case 0: expect(scope.model.update).toHaveBeenCalled(); break;
        case 1: expect(scope.model.remove).toHaveBeenCalled(); break;
        }
      });
    });

    it('only update button displayed when only one enabled', function() {
      var buttons, scope = createScope({ updateAllowed: true, removeAllowed: false});

      buttons = element.find('button');

      expect(buttons.length).toBe(1);

      buttons.eq(0).click();

      expect(scope.model.update).toHaveBeenCalled();
      expect(scope.model.remove).not.toHaveBeenCalled();
    });

    it('only remove button displayed when only one enabled', function() {
      var buttons, scope = createScope({ updateAllowed: false, removeAllowed: true});

      buttons = element.find('button');

      expect(buttons.length).toBe(1);

      buttons.eq(0).click();

      expect(scope.model.update).not.toHaveBeenCalled();
      expect(scope.model.remove).toHaveBeenCalled();
    });

    it('buttons should have valid icons', function() {
      var icons;

      createScope({ updateAllowed: true, removeAllowed: true});
      icons = element.find('button i');

      expect(icons.length).toBe(2);
      expect(icons.eq(0)).toHaveClass('glyphicon-edit');
      expect(icons.eq(1)).toHaveClass('glyphicon-remove');
    });

    it('buttons should have valid titles', function() {
      var buttons;

      createScope({ updateAllowed: true, removeAllowed: true});
      buttons = element.find('button');

      expect(buttons.length).toBe(2);
      expect(buttons.eq(0).attr('title')).toBe('Update');
      expect(buttons.eq(1).attr('title')).toBe('Remove');
    });

  });

});
