/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import filtersSharedBehaviour from '../../../test/filtersSharedBehaviour';

describe('Component: nameFilter', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q', '$rootScope', '$compile', 'factory');
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

        _.extend(actualBindings, defaultBindings, bindings);

        ComponentTestSuiteMixin.createController.call(
          this,
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
