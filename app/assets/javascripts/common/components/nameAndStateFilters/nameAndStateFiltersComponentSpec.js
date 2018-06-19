/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import filtersSharedBehaviour from 'test/behaviours/filtersSharedBehaviour';
import ngModule from '../../../app';

describe('nameAndStateFiltersComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);
      this.injectDependencies('$q', '$rootScope', '$compile', 'Factory');

      this.createController = (bindings) => {
        var defaultBindings = {},
            actualBindings = {};

        this.nameFilterUpdated = jasmine.createSpy().and.returnValue(null);
        this.stateFilterUpdated = jasmine.createSpy().and.returnValue(null);
        this.filtersCleared = jasmine.createSpy().and.returnValue(null);

        defaultBindings = {
          stateData:            [ 'enabled', 'disbled' ],
          onNameFilterUpdated:  this.nameFilterUpdated,
          onStateFilterUpdated: this.stateFilterUpdated,
          onFiltersCleared:     this.filtersCleared
        };

        Object.assign(actualBindings, defaultBindings, bindings);

        this.createControllerInternal(
          `<name-and-state-filters
              state-data="vm.stateData"
              on-name-filter-updated="vm.onNameFilterUpdated"
              on-state-filter-updated="vm.onStateFilterUpdated"
              on-filters-cleared="vm.onFiltersCleared">
            </name-and-state-filters>`,
          actualBindings,
          'nameAndStateFilters');
      };
    });
  });

  describe('for name filter', function() {
    var context = {};

    beforeEach(function() {
      context.createController = this.createController.bind(this);
    });

    filtersSharedBehaviour.nameFiltersharedBehaviour(context);

  });

  describe('for state filter', function() {
    var context = {};

    beforeEach(function() {
      context.createController = this.createController.bind(this);
    });

    filtersSharedBehaviour.stateFiltersharedBehaviour(context);

  });

});
