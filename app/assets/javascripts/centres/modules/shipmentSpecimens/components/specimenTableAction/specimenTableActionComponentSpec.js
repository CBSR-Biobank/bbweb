/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('Component: specimenTableAction', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Factory');
      this.addCustomMatchers();

      this.createController = (action, onActionSelected) => {
        ComponentTestSuiteMixin.createController.call(
          this,
          `<specimen-table-action
             action="action"
             on-action-selected="vm.onActionSelected">
            </specimen-table-action>`,
          {
            action:           action,
            onActionSelected: onActionSelected
          },
          'specimenTableAction');
      };
    });
  });

  it('has valid scope', function() {
    var action = { id:    'remove',
                   class: 'btn-warning',
                   title: 'Remove specimen',
                   icon:  'glyphicon-remove'
                 },
        onActionSelected = jasmine.createSpy('onActionSelected').and.returnValue(null);
    this.createController(action, onActionSelected);

    expect(this.scope.vm.action).toBe(action);
    expect(this.scope.vm.onActionSelected).toBeFunction();
  });

  it('can invoke callback function', function() {
    var action = {},
        onActionSelected = jasmine.createSpy('onActionSelected').and.returnValue(null);
    this.createController(action, onActionSelected);

    this.scope.vm.onActionSelected();
    this.scope.$digest();
    expect(onActionSelected).toHaveBeenCalled();
  });

});
