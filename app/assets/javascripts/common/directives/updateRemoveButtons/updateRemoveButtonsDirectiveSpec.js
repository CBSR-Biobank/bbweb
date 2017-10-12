/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Directive: updateRemoveButtons', function() {
  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (DirectiveTestSuiteMixin, TestUtils) {
      _.extend(this, DirectiveTestSuiteMixin);

      this.injectDependencies('$rootScope', '$compile');

      TestUtils.addCustomMatchers();

      this.createController = (options) => {
        options = options || {};

        DirectiveTestSuiteMixin.createController.call(
          this,
          `<update-remove-buttons
             on-update="vm.update()"
             on-remove="vm.remove()"
             update-button-enabled="vm.updateAllowed"
             remove-button-enabled="vm.removeAllowed">
          </update-remove-buttons>`,
          {
            update: jasmine.createSpy().and.returnValue(null),
            remove: jasmine.createSpy().and.returnValue(null),
            updateAllowed: options.updateAllowed || false,
            removeAllowed: options.removeAllowed || false
          });
      };
    });
  });

  it('clicking on a button invokes corresponding function', function() {
    this.createController({ updateAllowed: true, removeAllowed: true});

    const buttons = this.element.find('button');
    expect(buttons.length).toBe(2);
    _.range(buttons.length).forEach((i) => {
      buttons.eq(i).click();
      switch (i) {
      case 0: expect(this.controller.update).toHaveBeenCalled(); break;
      case 1: expect(this.controller.remove).toHaveBeenCalled(); break;
      }
    });
  });

  it('only update button displayed when only one enabled', function() {
    var buttons;

    this.createController({ updateAllowed: true, removeAllowed: false });

    buttons = this.element.find('button');
    expect(buttons.length).toBe(1);

    buttons.eq(0).click();
    expect(this.controller.update).toHaveBeenCalled();
    expect(this.controller.remove).not.toHaveBeenCalled();
  });

  it('only remove button displayed when only one enabled', function() {
    var buttons;

    this.createController({ updateAllowed: false, removeAllowed: true});

    buttons = this.element.find('button');
    expect(buttons.length).toBe(1);

    buttons.eq(0).click();

    expect(this.controller.update).not.toHaveBeenCalled();
    expect(this.controller.remove).toHaveBeenCalled();
  });

  it('buttons should have valid icons', function() {
    var icons;

    this.createController({ updateAllowed: true, removeAllowed: true});
    icons = this.element.find('button i');

    expect(icons.length).toBe(2);
    expect(icons.eq(0)).toHaveClass('glyphicon-edit');
    expect(icons.eq(1)).toHaveClass('glyphicon-remove');
  });

  it('buttons should have valid titles', function() {
    var buttons;

    this.createController({ updateAllowed: true, removeAllowed: true});
    buttons = this.element.find('button');

    expect(buttons.length).toBe(2);
    expect(buttons.eq(0).attr('uib-tooltip')).toBe('Update');
    expect(buttons.eq(1).attr('uib-tooltip')).toBe('Remove');
  });

});
