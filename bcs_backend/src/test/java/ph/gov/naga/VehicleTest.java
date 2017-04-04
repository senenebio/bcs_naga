package ph.gov.naga;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import ph.gov.naga.controller.internal.VehicleController;
import ph.gov.naga.model.Vehicle;
import ph.gov.naga.repository.VehicleRepository;
import ph.gov.naga.service.VehicleService;

/**
 *
 * @author Drei
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class VehicleTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VehicleController controller;

    @Autowired
    private VehicleService service;
    
    @Autowired
    private VehicleRepository repository;

    public VehicleTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {

        repository.deleteAll();

        Vehicle v = new Vehicle();
        v.setId(1L);
        v.setPlateNumber("123");
        v.setBodyNumber("123");
        v.setBusCompany("test");

        repository.saveAndFlush(v);
    }

    @After
    public void tearDown() {

    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void test() throws IOException {
        
        Vehicle v = service.findByPlateNumber("123");
        
        
        assertThat(v.getPlateNumber()).isNotNull();


        String body = this.restTemplate.getForObject("/internal/api/vehicle/" + v.getId(),
                String.class);
        System.out.print(body);

        
        ObjectMapper objectMapper = new ObjectMapper();
        Vehicle v2 = objectMapper.readValue(body, Vehicle.class);
        assertThat(v.toString()).isEqualTo(v2.toString());

        v.setBusCompany("edited");
        service.save(v);
        v = service.findByPlateNumber("123");
        assertThat(v.getBusCompany()).isEqualTo("edited");
    }
}
