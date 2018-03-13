/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'
import sharedBehaviour from '../../../../../test/behaviours/EntityPagedListSharedBehaviourSpec';

describe('centresPagedListComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Centre',
                              'CentreCounts',
                              'CentreState',
                              'NameFilter',
                              'StateFilter',
                              '$state',
                              'resourceErrorService',
                              'Factory');

      this.createController = () => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<centres-paged-list></centres-paged-list>',
          undefined,
          'centresPagedList');
      };

      this.createCountsSpy = (disabled, enabled) => {
        var counts = {
          total:    disabled + enabled,
          disabled: disabled,
          enabled:  enabled
        };

        spyOn(this.CentreCounts, 'get').and.returnValue(this.$q.when(counts));
      };

      this.createEntity = () => {
        var entity = new this.Centre(this.Factory.centre());
        return entity;
      };

      this.createPagedResultsSpy = (centres) => {
        var reply = this.Factory.pagedResult(centres);
        spyOn(this.Centre, 'list').and.returnValue(this.$q.when(reply));
      };

    });
  });

  it('scope is valid on startup', function() {
    this.createCountsSpy(2, 5);
    this.createPagedResultsSpy([]);
    this.createController();

    expect(this.controller.limit).toBeDefined();
    expect(this.controller.filters.stateFilter.allChoices()).toBeArrayOfObjects();
    expect(this.controller.filters.stateFilter.allChoices()).toBeNonEmptyArray();
    expect(this.controller.getItems).toBeFunction();
    expect(this.controller.getItemIcon).toBeFunction();
  });

  it('when centre counts call fails', function() {
    const errFunc = jasmine.createSpy().and.returnValue(null);
    this.resourceErrorService.checkUnauthorized = jasmine.createSpy().and.returnValue(errFunc);
    this.CentreCounts.get = jasmine.createSpy()
      .and.returnValue(this.$q.reject({ status: 401, message: 'testing'}));
    this.createController();
    expect(errFunc).toHaveBeenCalled();
  })

  describe('centres', function () {

    var context = {};

    beforeEach(function () {
      context.createController = (centresCount) => {
        centresCount = centresCount || 0;
        const centres = _.range(centresCount).map(() => this.createEntity());
        this.createCountsSpy(2, 5);
        this.createPagedResultsSpy(centres);
        this.createController();
      };

      context.getEntitiesLastCallArgs = () => this.Centre.list.calls.mostRecent().args;

      context.stateFilterValue = this.CentreState.DISABLED;
      context.validFilters = [ 'nameFilter', 'stateFilter' ]
      context.sortFieldIds = ['name', 'state'];
      context.defaultSortFiled = 'name';
    });

    sharedBehaviour(context);

  });

  describe('getItemIcon', function() {

    beforeEach(function() {
      this.createCountsSpy(2, 5);
      this.createPagedResultsSpy([]);
      this.createController();
    });

    it('getItemIcon returns a valid icon', function() {
      var self = this,
          statesInfo = [
            { state: this.CentreState.DISABLED, icon: 'glyphicon-cog' },
            { state: this.CentreState.ENABLED,  icon: 'glyphicon-ok-circle' }
          ];

      statesInfo.forEach(function (info) {
        var centre = new self.Centre(self.Factory.centre({ state: info.state }));
        expect(self.controller.getItemIcon(centre)).toEqual(info.icon);
      });
    });

    it('getItemIcon throws an error for and invalid state', function() {
      var self = this,
          centre = new this.Centre(this.Factory.centre({ state: this.Factory.stringNext() }));

      expect(function () {
        self.controller.getItemIcon(centre);
      }).toThrowError(/invalid centre state/);
    });

  });

});
