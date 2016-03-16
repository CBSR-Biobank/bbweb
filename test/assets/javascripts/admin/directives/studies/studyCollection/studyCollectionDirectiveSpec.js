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

  describe('studyCollectionDirective', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($rootScope, $compile, directiveTestSuite, testUtils) {
      var self = this, jsonStudy, jsonCet;

      _.extend(self, directiveTestSuite);

      self.$q                   = self.$injector.get('$q');
      self.Study                = self.$injector.get('Study');
      self.CollectionEventType  = self.$injector.get('CollectionEventType');
      self.jsonEntities         = self.$injector.get('jsonEntities');

      jsonStudy = self.jsonEntities.study();
      jsonCet   = self.jsonEntities.collectionEventType(jsonStudy);

      self.study = new self.Study(jsonStudy);
      self.collectionEventType = new self.CollectionEventType(jsonCet);
      self.createDirective = setupDirective();

      spyOn(self.CollectionEventType, 'list').and.returnValue(self.$q.when([ self.collectionEventType ]));

      self.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/studyCollection/studyCollection.html',
        '/assets/javascripts/admin/directives/studies/studyNotDisabledWarning/studyNotDisabledWarning.html',
        '/assets/javascripts/admin/directives/studies/collection/ceventTypesAddAndSelect/ceventTypesAddAndSelect.html');

      function setupDirective() {
        return create;

        function create() {
          self.element = angular.element([
            '<study-collection',
            '   study="study">',
            '</study-collection>'
          ].join(''));
          self.scope = $rootScope.$new();
          self.scope.study = self.study;

          $compile(self.element)(self.scope);
          self.scope.$digest();
        }
      }
    }));

    it('has valid scope', function() {
      this.createDirective();
      expect(this.scope.study).toBe(this.study);
    });

  });

});
