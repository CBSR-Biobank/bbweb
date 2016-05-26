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

  describe('Directive: infoUpdateRemoveButtons', function() {
    var rootScope, compile, element,
        buttonClickFuncNames = ['information', 'update', 'remove'];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($rootScope, $compile, templateMixin, testUtils) {
      _.extend(this, templateMixin);

      rootScope = $rootScope;
      compile = $compile;

      this.putHtmlTemplates(
        '/assets/javascripts/common/directives/infoUpdateRemoveButtons.html');

      element  = angular.element(
        '<info-update-remove-buttons' +
          '   on-info="model.information()"' +
          '   on-update="model.update()"' +
          '   on-remove="model.remove()"' +
          '   update-button-enabled="model.updateAllowed"' +
          '   remove-button-enabled="model.removeAllowed">' +
          '</info-update-remove-buttons>');
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
      var buttons,
          scope = createScope({ updateAllowed: true, removeAllowed: true});

      buttons = element.find('button');

      expect(buttons.length).toBe(3);
      _.each(_.range(buttons.length), function (i) {
        buttons.eq(i).click();
        switch (i) {
        case 0: expect(scope.model.information).toHaveBeenCalled(); break;
        case 1: expect(scope.model.update).toHaveBeenCalled(); break;
        case 2: expect(scope.model.remove).toHaveBeenCalled(); break;
        }
      });
    });

    it('only one button displayed if updateAllowed and removeAllowed are false', function() {
      var buttons,
          scope = createScope({ updateAllowed: false, removeAllowed: false});

      buttons = element.find('button');

      expect(buttons.length).toBe(1);

      buttons.eq(0).click();

      expect(scope.model.information).toHaveBeenCalled();
      expect(scope.model.update).not.toHaveBeenCalled();
      expect(scope.model.remove).not.toHaveBeenCalled();
    });

    it('buttons should have valid icons', function() {
      var icons;

      createScope({ updateAllowed: true, removeAllowed: true});
      icons = element.find('button i');

      expect(icons.length).toBe(3);
      expect(icons.eq(0)).toHaveClass('glyphicon-info-sign');
      expect(icons.eq(1)).toHaveClass('glyphicon-edit');
      expect(icons.eq(2)).toHaveClass('glyphicon-remove');
    });

    it('buttons should have valid titles', function() {
      var buttons;

      createScope({ updateAllowed: true, removeAllowed: true});
      buttons = element.find('button');

      expect(buttons.length).toBe(3);
      expect(buttons.eq(0).attr('title')).toBe('More information');
      expect(buttons.eq(1).attr('title')).toBe('Update');
      expect(buttons.eq(2).attr('title')).toBe('Remove');
    });

  });

});
