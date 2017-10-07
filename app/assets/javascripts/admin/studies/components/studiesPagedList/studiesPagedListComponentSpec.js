/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import sharedBehaviour from '../../../../test/EntityPagedListSharedBehaviourSpec';

describe('Component: studiesPagedList', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Study',
                              'StudyCounts',
                              'StudyState',
                              'NameFilter',
                              'StateFilter',
                              '$state',
                              'factory');

      this.createController = () => {
        ComponentTestSuiteMixin.prototype.createController.call(
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
        var reply = this.factory.pagedResult(studies);
        spyOn(this.Study, 'list').and.returnValue(this.$q.when(reply));
      };

      this.createEntity = () => {
        var entity = new this.Study(this.factory.study());
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
    this.StudyCounts.get = jasmine.createSpy()
      .and.returnValue(this.$q.reject({ status: 400, message: 'testing'}));
    this.createController();
    expect(this.controller.counts).toEqual({});
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
        var study = new self.Study(self.factory.study({ state: info.state }));
        expect(self.controller.getItemIcon(study)).toEqual(info.icon);
      });
    });

    it('getItemIcon throws an error for and invalid state', function() {
      var self = this,
          study = new this.Study(this.factory.study({ state: this.factory.stringNext() }));

      expect(function () {
        self.controller.getItemIcon(study);
      }).toThrowError(/invalid study state/);
    });

  });

});
