/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import filtersSharedBehaviour from 'test/behaviours/filtersSharedBehaviour';
import ngModule from '../../../app'

describe('Component: nameFilter', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q', '$rootScope', '$compile', 'Factory');
      this.createController = (bindings) => {
        var self = this,
            defaultBindings = {},
            actualBindings = {};

        self.nameFilterUpdated = jasmine.createSpy().and.returnValue(null);
        self.filtersCleared = jasmine.createSpy().and.returnValue(null);

        defaultBindings = {
          stateData:            [ 'enabled', 'disbled' ],
          onNameFilterUpdated:  self.nameFilterUpdated,
          onStateFilterUpdated: self.stateFilterUpdated,
          onFiltersCleared:     self.filtersCleared
        };

        Object.assign(actualBindings, defaultBindings, bindings);

        this.createControllerInternal(
          [
            '<name-filter ',
            '    on-name-filter-updated="vm.onNameFilterUpdated" ',
            '    on-filters-cleared="vm.onFiltersCleared"> ',
            '</name-filter>'
          ].join(''),
          actualBindings,
          'nameFilter');
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

});
