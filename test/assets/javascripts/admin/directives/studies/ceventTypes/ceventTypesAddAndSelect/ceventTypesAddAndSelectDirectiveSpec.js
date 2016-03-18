/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('ceventTypesAddAndSelectDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this, jsonStudy, jsonCet;

      _.extend(self, directiveTestSuite);

      self.$q                   = self.$injector.get('$q');
      self.$state               = self.$injector.get('$state');
      self.Study                = self.$injector.get('Study');
      self.CollectionEventType  = self.$injector.get('CollectionEventType');
      self.jsonEntities         = self.$injector.get('jsonEntities');

      jsonStudy = self.jsonEntities.study();
      jsonCet   = self.jsonEntities.collectionEventType(jsonStudy);

      self.study = new self.Study(jsonStudy);
      self.collectionEventType = new self.CollectionEventType(jsonCet);
      self.createController = createController;

      spyOn(self.CollectionEventType, 'list').and.returnValue(self.$q.when([ self.collectionEventType ]));
      spyOn(this.$state, 'go').and.callFake(function () {});

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/collection/ceventTypesAddAndSelect/ceventTypesAddAndSelect.html');

      function createController() {
        self.element = angular.element([
          '<cevent-types-add-and-select',
          '   study="vm.study">',
          '</cevent-types-add-and-select>'
        ].join(''));
        self.scope = $rootScope.$new();
        self.scope.vm = { study: self.study };

        $compile(self.element)(self.scope);
        self.scope.$digest();
        self.controller = self.element.controller('ceventTypesAddAndSelect');
      }
    }));

    it('has valid scope', function() {
      this.createController();
      expect(this.controller.study).toBe(this.study);
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

  });

});
