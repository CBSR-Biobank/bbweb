/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Directive: infoUpdateRemoveButtons', function() {
  var buttonClickFuncNames = ['information', 'update', 'remove'];

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);
      this.injectDependencies('$rootScope', '$compile');

      this.element  = angular.element(
        `<info-update-remove-buttons
           on-info="model.information()"
           on-update="model.update()"
           on-remove="model.remove()"
           update-button-enabled="model.updateAllowed"
           remove-button-enabled="model.removeAllowed">
         </info-update-remove-buttons>`);

      this.createScope = (options) => {
        options = options || {};
        this.scope = this.$rootScope.$new();
        this.scope.model = {};
        this.scope.model.updateAllowed = options.updateAllowed || false;
        this.scope.model.removeAllowed = options.removeAllowed || false;

        buttonClickFuncNames.forEach((key) => {
          this.scope.model[key] = angular.noop;
          spyOn(this.scope.model, key).and.returnValue(key);
        });

        this.$compile(this.element)(this.scope);
        this.scope.$digest();
      };
    });
  });

  it('clicking on a button invokes corresponding function', function() {
    var buttons;

    this.createScope({ updateAllowed: true, removeAllowed: true});
    buttons = this.element.find('button');
    expect(buttons.length).toBe(3);
    _.range(buttons.length).forEach((i) => {
      buttons.eq(i).click();
      switch (i) {
      case 0: expect(this.scope.model.information).toHaveBeenCalled(); break;
      case 1: expect(this.scope.model.update).toHaveBeenCalled(); break;
      case 2: expect(this.scope.model.remove).toHaveBeenCalled(); break;
      }
    });
  });

  it('only one button displayed if updateAllowed and removeAllowed are false', function() {
    var buttons;

    this.createScope({ updateAllowed: false, removeAllowed: false});

    buttons = this.element.find('button');
    expect(buttons.length).toBe(1);
    buttons.eq(0).click();

    expect(this.scope.model.information).toHaveBeenCalled();
    expect(this.scope.model.update).not.toHaveBeenCalled();
    expect(this.scope.model.remove).not.toHaveBeenCalled();
  });

  it('buttons should have valid icons', function() {
    var icons;

    this.createScope({ updateAllowed: true, removeAllowed: true});
    icons = this.element.find('button i');

    expect(icons.length).toBe(3);
    expect(icons.eq(0)).toHaveClass('glyphicon-info-sign');
    expect(icons.eq(1)).toHaveClass('glyphicon-edit');
    expect(icons.eq(2)).toHaveClass('glyphicon-remove');
  });

  it('buttons should have valid titles', function() {
    var buttons;

    this.createScope({ updateAllowed: true, removeAllowed: true});
    buttons = this.element.find('button');

    expect(buttons.length).toBe(3);
    expect(buttons.eq(0).attr('uib-tooltip')).toBe('More information');
    expect(buttons.eq(1).attr('uib-tooltip')).toBe('Update');
    expect(buttons.eq(2).attr('uib-tooltip')).toBe('Remove');
  });

});
