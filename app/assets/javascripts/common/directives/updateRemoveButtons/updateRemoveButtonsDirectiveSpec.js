/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Directive: updateRemoveButtons', function() {
  var buttonClickFuncNames = ['update', 'remove'];

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (TestSuiteMixin, testUtils) {
      _.extend(this, TestSuiteMixin.prototype);

      this.injectDependencies('$rootScope', '$compile');

      testUtils.addCustomMatchers();
      this.createScope = (options) => {
        options = options || {};

        this.element = angular.element(
          `<update-remove-buttons
             on-update="model.update()"
             on-remove="model.remove()"
             update-button-enabled="model.updateAllowed"
             remove-button-enabled="model.removeAllowed">
          </update-remove-buttons>`);

        this.scope = this.$rootScope.$new();
        this.scope.model = {};
        this.scope.model.updateAllowed = options.updateAllowed || false;
        this.scope.model.removeAllowed = options.removeAllowed || false;

        buttonClickFuncNames.forEach((key) => {
          this.scope.model[key] = function () {};
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
    expect(buttons.length).toBe(2);
    _.range(buttons.length).forEach((i) => {
      buttons.eq(i).click();
      switch (i) {
      case 0: expect(this.scope.model.update).toHaveBeenCalled(); break;
      case 1: expect(this.scope.model.remove).toHaveBeenCalled(); break;
      }
    });
  });

  it('only update button displayed when only one enabled', function() {
    var buttons;

    this.createScope({ updateAllowed: true, removeAllowed: false });

    buttons = this.element.find('button');
    expect(buttons.length).toBe(1);

    buttons.eq(0).click();
    expect(this.scope.model.update).toHaveBeenCalled();
    expect(this.scope.model.remove).not.toHaveBeenCalled();
  });

  it('only remove button displayed when only one enabled', function() {
    var buttons;

    this.createScope({ updateAllowed: false, removeAllowed: true});

    buttons = this.element.find('button');
    expect(buttons.length).toBe(1);

    buttons.eq(0).click();

    expect(this.scope.model.update).not.toHaveBeenCalled();
    expect(this.scope.model.remove).toHaveBeenCalled();
  });

  it('buttons should have valid icons', function() {
    var icons;

    this.createScope({ updateAllowed: true, removeAllowed: true});
    icons = this.element.find('button i');

    expect(icons.length).toBe(2);
    expect(icons.eq(0)).toHaveClass('glyphicon-edit');
    expect(icons.eq(1)).toHaveClass('glyphicon-remove');
  });

  it('buttons should have valid titles', function() {
    var buttons;

    this.createScope({ updateAllowed: true, removeAllowed: true});
    buttons = this.element.find('button');

    expect(buttons.length).toBe(2);
    expect(buttons.eq(0).attr('uib-tooltip')).toBe('Update');
    expect(buttons.eq(1).attr('uib-tooltip')).toBe('Remove');
  });

});
