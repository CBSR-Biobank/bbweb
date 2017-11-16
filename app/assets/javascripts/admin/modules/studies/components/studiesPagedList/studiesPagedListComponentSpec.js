/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'
import sharedBehaviour from '../../../../../test/behaviours/EntityPagedListSharedBehaviourSpec';

describe('Component: studiesPagedList', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'StudyCounts',
                              'StudyState',
                              'NameFilter',
                              'StateFilter',
                              'resourceErrorService',
                              '$state',
                              'Factory');

      this.createController = () => {
        ComponentTestSuiteMixin.createController.call(
          this,
          '<studies-paged-list></studies-paged-list',
          undefined,
          'studiesPagedList');
      };

      this.createCountsSpy = (disabled, enabled, retired) => {
        var counts = {
          total:    disabled + enabled + retired,
          disabled: disabled,
          enabled:  enabled,
          retired:  retired
        };

        spyOn(this.StudyCounts, 'get').and.returnValue(this.$q.when(counts));
      };

      this.createPagedResultsSpy = (studies) => {
        var reply = this.Factory.pagedResult(studies);
        spyOn(this.Study, 'list').and.returnValue(this.$q.when(reply));
      };

      this.createEntity = () => {
        var entity = new this.Study(this.Factory.study());
        return entity;
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

  it('when study counts fails', function() {
    const errFunc = jasmine.createSpy().and.returnValue(null);
    this.resourceErrorService.checkUnauthorized = jasmine.createSpy().and.returnValue(errFunc);
    this.StudyCounts.get = jasmine.createSpy()
      .and.returnValue(this.$q.reject({ status: 400, message: 'testing'}));
    this.createController();
    expect(errFunc).toHaveBeenCalled();
  });

  describe('studies', function () {

    var context = {};

    beforeEach(function () {
      context.createController = (studiesCount) => {
        studiesCount = studiesCount || 0;
        const studies = _.range(studiesCount).map(() => this.createEntity());
        this.createCountsSpy(2, 5, 3);
        this.createPagedResultsSpy(studies);
        this.createController();
      };

      context.getEntitiesLastCallArgs = () => this.Study.list.calls.mostRecent().args;

      context.stateFilterValue = this.StudyState.DISABLED;
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
            { state: this.StudyState.DISABLED, icon: 'glyphicon-cog' },
            { state: this.StudyState.ENABLED,  icon: 'glyphicon-ok-circle' },
            { state: this.StudyState.RETIRED,  icon: 'glyphicon-remove-sign' }
          ];

      statesInfo.forEach(function (info) {
        var study = new self.Study(self.Factory.study({ state: info.state }));
        expect(self.controller.getItemIcon(study)).toEqual(info.icon);
      });
    });

    it('getItemIcon throws an error for and invalid state', function() {
      var self = this,
          study = new this.Study(this.Factory.study({ state: this.Factory.stringNext() }));

      expect(function () {
        self.controller.getItemIcon(study);
      }).toThrowError(/invalid study state/);
    });

  });

});
