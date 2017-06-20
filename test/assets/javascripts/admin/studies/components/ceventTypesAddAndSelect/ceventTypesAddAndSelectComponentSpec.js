/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('ceventTypesAddAndSelectComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      var self = this, jsonStudy, jsonCet;

      _.extend(self, TestSuiteMixin.prototype);

      self.injectDependencies('$rootScope',
                              '$compile',
                              '$q',
                              '$state',
                              'Study',
                              'CollectionEventType',
                              'factory');

      jsonStudy = self.factory.study();
      jsonCet   = self.factory.collectionEventType(jsonStudy);

      self.study = new self.Study(jsonStudy);
      self.collectionEventType = new self.CollectionEventType(jsonCet);

      spyOn(self.CollectionEventType, 'list').and.returnValue(self.$q.when([ self.collectionEventType ]));
      spyOn(this.$state, 'go').and.callFake(function () {});

      self.putHtmlTemplates(
        '/assets/javascripts/admin/studies/components/ceventTypesAddAndSelect/ceventTypesAddAndSelect.html');

      this.createController = function (study, collectionEventType) {
        var collectionEventTypes;
        study = study || self.study;
        collectionEventType = collectionEventType || self.collectionEventType;

        if (_.isUndefined(collectionEventType)) {
          collectionEventTypes = [];
        } else {
          collectionEventTypes = [ collectionEventType ];
        }

        self.CollectionEventType.list =
          jasmine.createSpy().and.returnValue(self.$q.when(self.factory.pagedResult(collectionEventTypes)));

        self.element = angular.element(
          '<cevent-types-add-and-select study="vm.study" collection-event-types="vm.collectionEventTypes">' +
            '</cevent-types-add-and-select>');
        self.scope = self.$rootScope.$new();
        self.scope.vm = {
          study:                study,
          collectionEventTypes: collectionEventType
        };

        self.$compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('ceventTypesAddAndSelect');
      };
    }));

    it('has valid scope', function() {
      this.collectionEventType = undefined;
      this.createController(this.study, undefined);
      expect(this.controller.collectionEventTypes).toBeArrayOfSize(0);
    });

    it('function add switches to correct state', function() {
      this.createController();
      this.controller.add();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection.ceventTypeAdd');
    });

    it('function select switches to correct state', function() {
      this.createController();
      this.controller.select(this.collectionEventType);
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection.ceventType',
                                                  { ceventTypeId: this.collectionEventType.id });
    });

    it('function recurring returns a valid result', function() {
      this.createController();
      this.collectionEventType.recurring = false;
      expect(this.controller.getRecurringLabel(this.collectionEventType)).toBe('Not recurring');
      this.collectionEventType.recurring = true;
      expect(this.controller.getRecurringLabel(this.collectionEventType)).toBe('Recurring');
    });

    it('function pageChanged switches to correct state', function() {
      this.createController();
      this.controller.pageChanged();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.admin.studies.study.collection');
    });

    describe('for updating name filter', function() {

      it('filter is updated when user enters a value', function() {
        var name = this.factory.stringNext();
        this.createController();

        this.CollectionEventType.list =
          jasmine.createSpy().and.returnValue(this.$q.when(this.factory.pagedResult([])));

        this.controller.nameFilter = name;
        this.controller.nameFilterUpdated();
        this.scope.$digest();

        expect(this.controller.pagerOptions.filter).toEqual('name:like:' + name);
        expect(this.controller.pagerOptions.page).toEqual(1);
        expect(this.controller.displayState).toBe(this.controller.displayStates.NO_RESULTS);
      });

      it('filter is updated when user clears the value', function() {
        this.createController();
        this.controller.nameFilter = '';
        this.controller.nameFilterUpdated();
        this.scope.$digest();

        expect(this.controller.pagerOptions.filter).toBeEmptyString();
        expect(this.controller.pagerOptions.page).toEqual(1);
      });

    });

  });

});
