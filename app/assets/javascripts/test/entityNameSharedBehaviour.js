/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
   'use strict';

   var _       = require('lodash'),
       sprintf = require('sprintf-js').sprintf;

   /**
    * @param {object} context The context to use to test the behaviour. See below for required fields.
    *
    * @param {constructor} context.constructor The constructor function for the entity to test.
    *
    * @param {function} context.createFunc The static function that creates the entity name.
    *
    * @param {string} context.restApiUrl The URL for the REST API.
    *
    * @param {function} context.factoryFunc The function in factory.js that creates a plain object of the
    * entity to test.
    *
    * @param {function} context.listFunc The static function that invokes the REST API to list the entities.
    *
    * @return {null} nothing
    */
   function entityNameSharedBehaviour(context) {

      it('constructor with no parameters has default values', function() {
         var entityName = new context.constructor();
         expect(entityName.id).toBeNull();
         expect(entityName.name).toBeNull();
      });

      it('fails when creating from an invalid object', function() {
         this.EntityName.SCHEMA.required.forEach(function (field) {
            var badStudyJson = _.omit(context.factoryFunc(), field);

            expect(function () {
               context.createFunc(badStudyJson);
            }).toThrowError(/Missing required property/);
         });
      });

      describe('when listing study names', function() {

         it('can retrieve entity names', function() {
            var names = [ context.factoryFunc() ];

            this.$httpBackend.whenGET(context.restApiUrl).respond(this.reply(names));
            context.listFunc().then(testEntity).catch(failTest);
            this.$httpBackend.flush();

            function testEntity(reply) {
               expect(reply).toBeArrayOfSize(1);
               expect(reply[0]).toEqual(jasmine.any(context.constructor));
            }
         });

         it('can use options', function() {
            var self = this,
                optionList = [
                   { filter: 'name::test' },
                   { sort: 'state' }
                ],
                names = [ context.factoryFunc() ];

            _.each(optionList, function (options) {
               var url = sprintf('%s?%s', context.restApiUrl, self.$httpParamSerializer(options, true));

               self.$httpBackend.whenGET(url).respond(self.reply(names));
               context.listFunc(options).then(testEntity).catch(failTest);
               self.$httpBackend.flush();
            });

            function testEntity(reply) {
               expect(reply).toBeArrayOfSize(names.length);
               _.each(reply, function (study) {
                  expect(study).toEqual(jasmine.any(context.constructor));
               });
            }
         });

         it('listing omits empty options', function() {
            var self    = this,
                options = { filter: ''},
                names = [ context.factoryFunc() ];

            this.$httpBackend.whenGET(context.restApiUrl).respond(this.reply(names));
            context.listFunc(options).then(testEntity).catch(failTest);
            this.$httpBackend.flush();

            function testEntity(reply) {
               expect(reply).toBeArrayOfSize(names.length);
               _.each(reply, function (study) {
                  expect(study).toEqual(jasmine.any(self.EntityName));
               });
            }
         });

         it('fails when an invalid study is returned', function() {
            var self = this,
                names = [ _.omit(context.factoryFunc(), 'name') ];

            self.$httpBackend.whenGET(context.restApiUrl).respond(this.reply(names));
            context.listFunc().then(listFail).catch(shouldFail);
            self.$httpBackend.flush();

            function listFail() {
               fail('function should not be called');
            }

            function shouldFail(error) {
               expect(error).toMatch('invalid.*names from server');
            }
         });

      });

      // used by promise tests
      function failTest(error) {
         expect(error).toBeUndefined();
      }

   }

   return entityNameSharedBehaviour;

});
