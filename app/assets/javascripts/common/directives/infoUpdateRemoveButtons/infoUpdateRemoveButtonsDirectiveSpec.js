/*
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { DirectiveTestSuiteMixin } from 'test/mixins/DirectiveTestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../../app';

describe('Directive: infoUpdateRemoveButtons', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function () {
      Object.assign(this, DirectiveTestSuiteMixin);

      this.injectDependencies();

      this.createController = (options = {}) => {
        this.createControllerInternal(
          `<info-update-remove-buttons on-info="vm.information()"
                                       on-update="vm.update()"
                                       on-remove="vm.remove()"
                                       update-button-enabled="vm.updateAllowed"
                                       remove-button-enabled="vm.removeAllowed">
           </info-update-remove-buttons>`,
          {
            information: jasmine.createSpy().and.returnValue(null),
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
    expect(buttons.length).toBe(3);
    _.range(buttons.length).forEach((i) => {
      buttons.eq(i).click();
      switch (i) {
      case 0: expect(this.controller.information).toHaveBeenCalled(); break;
      case 1: expect(this.controller.update).toHaveBeenCalled(); break;
      case 2: expect(this.controller.remove).toHaveBeenCalled(); break;
      }
    });
  });

  it('only one button displayed if updateAllowed and removeAllowed are false', function() {
    this.createController({ updateAllowed: false, removeAllowed: false});

    const buttons = this.element.find('button');
    expect(buttons.length).toBe(1);
    buttons.eq(0).click();

    expect(this.controller.information).toHaveBeenCalled();
    expect(this.controller.update).not.toHaveBeenCalled();
    expect(this.controller.remove).not.toHaveBeenCalled();
  });

  it('buttons should have valid icons', function() {
    this.createController({ updateAllowed: true, removeAllowed: true});
    const icons = this.element.find('button i');
    expect(icons.length).toBe(3);
    expect(icons.eq(0)).toHaveClass('glyphicon-info-sign');
    expect(icons.eq(1)).toHaveClass('glyphicon-edit');
    expect(icons.eq(2)).toHaveClass('glyphicon-remove');
  });

  it('buttons should have valid titles', function() {
    this.createController({ updateAllowed: true, removeAllowed: true});
    const buttons = this.element.find('button');
    expect(buttons.length).toBe(3);
    expect(buttons.eq(0).attr('uib-tooltip')).toBe('More information');
    expect(buttons.eq(1).attr('uib-tooltip')).toBe('Update');
    expect(buttons.eq(2).attr('uib-tooltip')).toBe('Remove');
  });

});
