/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import filtersSharedBehaviour from '../../../test/behaviours/filtersSharedBehaviour';

describe('nameEmailStateFiltersComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);
      this.injectDependencies('$q', '$rootScope', '$compile', 'Factory');

      this.createController = (bindings) => {
        var defaultBindings = {},
            actualBindings = {};

        this.nameFilterUpdated = jasmine.createSpy().and.returnValue(null);
        this.emailFilterUpdated = jasmine.createSpy().and.returnValue(null);
        this.stateFilterUpdated = jasmine.createSpy().and.returnValue(null);
        this.filtersCleared = jasmine.createSpy().and.returnValue(null);

        defaultBindings = {
          stateData:            [ 'enabled', 'disbled' ],
          selectedState:        'all',
          onNameFilterUpdated:  this.nameFilterUpdated,
          onEmailFilterUpdated: this.emailFilterUpdated,
          onStateFilterUpdated: this.stateFilterUpdated,
          onFiltersCleared:     this.filtersCleared
        };

        Object.assign(actualBindings, defaultBindings, bindings);

        ComponentTestSuiteMixin.createController.call(
          this,
          `<name-email-state-filters
             state-data="vm.stateData"
             selected-state="${actualBindings.selectedState}"
             on-name-filter-updated="vm.onNameFilterUpdated"
             on-email-filter-updated="vm.onEmailFilterUpdated"
             on-state-filter-updated="vm.onStateFilterUpdated"
             on-filters-cleared="vm.onFiltersCleared">
           </name-email-state-filters>`,
          actualBindings,
          'nameEmailStateFilters');
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

  it('has valid scope', function() {
    this.createController();
    expect(this.controller.emailFilter).toBeEmptyString();
    expect(this.controller.emailFilterUpdated).toBeFunction();
  });

  it('invokes the callback when the email filter is updated', function() {
    this.createController();

    this.controller.emailFilter = 'test';
    this.controller.emailFilterUpdated();
    this.scope.$digest();

    expect(this.emailFilterUpdated).toHaveBeenCalled();
  });

  it('invokes the callback when the filters are cleared', function() {
    this.createController();
    this.controller.clearFilters();
    this.scope.$digest();
    expect(this.controller.emailFilter).toBeEmptyString();
    expect(this.filtersCleared).toHaveBeenCalled();
  });

});
