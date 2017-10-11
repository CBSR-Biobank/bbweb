/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  EntityTestSuiteFactory.$inject = ['$httpBackend', 'TestSuiteMixin'];

  function EntityTestSuiteFactory($httpBackend, TestSuiteMixin) {

    /**
     * A mixin for domain entity test suites.
     *
     * @mixin entityTestSuite
     */
    function EntityTestSuite() {
      TestSuiteMixin.call(this);
    }

    EntityTestSuite.prototype = Object.create(TestSuiteMixin);
    EntityTestSuite.prototype.constructor = EntityTestSuite;

    /**
     * @method
     * @memberof entityTestSuite
     * @instance
     *
     * Used to test updating an attribute on an entity.
     *
     * @param {Object} entity - An instance of the entity to be updated.
     *
     * @param {string} updateFuncName - The name of the update function on the entity to be tested.
     *
     * @param {(string[]|Object)} updateParams - the parameters to pass to the update function. If this is not
     * an string array, it will be converted to a single item array.
     *
     * @param {string} url - the URL on the server where the request will be POSTed to.
     *
     * @param {string} reqJson - the JSON to pass along with the POST request.
     *
     * @param {object} reply - The mocked reply from the server.
     *
     * @param {function(Object)} thenFunc the success function to be called if the update to the entity is
     * successfull.
     *
     * @param {function(string)} catchFunc the fail function to be called if the update to the entity fails.
     *
     * @param {boolean} [appendExpectedVersion] - If TRUE, the 'expectedVersion' field is added to the request
     *        JSON. Default value is TRUE.
     */
    EntityTestSuite.prototype.updateEntity = function (entity,
                                                       updateFuncName,
                                                       updateParams,
                                                       url,
                                                       reqJson,
                                                       reply,
                                                       thenFunc,
                                                       catchFunc,
                                                       appendExpectedVersion) {
      expect(entity[updateFuncName]).toBeFunction();
      expect(thenFunc).toBeFunction();
      expect(catchFunc).toBeFunction();

      appendExpectedVersion = _.isBoolean(appendExpectedVersion) ? appendExpectedVersion : true;
      if (appendExpectedVersion) {
        _.extend(reqJson, { expectedVersion: 0 });
      }
      $httpBackend.expectPOST(url, reqJson).respond(201, { status: 'success', data: reply });

      if (!Array.isArray(updateParams)) {
        updateParams = [ updateParams ];
      }

      entity[updateFuncName].apply(entity, updateParams).then(thenFunc).catch(catchFunc);
      $httpBackend.flush();
    };

    return EntityTestSuite;
  }

  return EntityTestSuiteFactory;

});
